# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

Yet Another 6502 Simulator (ya6s): a cycle-accurate simulator of a WDC 65C02S
microprocessor built on a digital circuit model (signals/busses), not an
instruction-level emulator. It passes Klaus Dormann's 6502/65C02 functional
test suites. An out-of-band monitor (REPL) is included for loading programs,
setting breakpoints, and inspecting the running system.

## Build & test commands

```
./gradlew build              # full build (compiles, tests, spotbugs, produces build/distributions/ya6s.tar|zip)
./gradlew test                # run all tests
./gradlew test --tests "org.joev.ya6s.AbsoluteTests"              # run one test class
./gradlew test --tests "org.joev.ya6s.AbsoluteTests.test"         # run one parameterized test method (all cases)
./gradlew spotbugsMain spotbugsTest   # static analysis (spotbugs-exclude.xml has suppressions)
./gradlew run --args="path/to/some.config"   # run the simulator/monitor against a config file
```

Requires JDK 17+ (compiled with `--release 22`); the module is
`org.joev.ya6s` (see `src/main/java/module-info.java`). Compiler args include
`-Xlint:all -Werror`, so warnings fail the build — fix them rather than
suppressing.

## Architecture

### Circuit simulation model (`org.joev.ya6s.signals`)

Everything is built on two primitives:
- `Signal`: a boolean wire. Setting its value fires `POSITIVE_EDGE` /
  `NEGATIVE_EDGE` events to registered `Listener`s only when the value
  actually changes.
- `Bus`: a multi-bit value (e.g. 16-bit address bus, 8-bit data bus) with no
  edge-notification semantics of its own.
- `OpenCollector` (extends `Signal`): used for wired-OR lines like `irqb`/`nmib`
  where multiple devices can independently assert.

### Backplane

`Backplane` is the shared set of signals/busses that mimic the real 65C02
pins: `address` (16-bit bus), `data` (8-bit bus), `rwb`, `sync`, `clock`,
`vpb`, `mlb`, `be`, `irqb`, `nmib`, `resb`, `rdy`. Devices never talk to each
other directly — they only communicate by reading/writing the Backplane's
signals and busses, exactly like real chips on a bus.

### Devices (`SRAM`, `ROM`, `UART`, `Counter`)

Each device's constructor takes `(Backplane, Map<String, String> options)`
and registers a listener on `backplane.clock()`. On the relevant clock edge,
a device checks the address bus to see if the address is within its range;
if so it reads/writes the data bus depending on `rwb`. This is also the
contract for user-defined devices attached at runtime via the monitor's
`attach` command — no interface/base class is required, just the constructor
signature and self-registration with the Backplane.

### CPU (`W65C02S`)

Implements the 65C02 as a big cycle-driven state machine reacting to clock
edges, not a fetch-decode-execute loop over whole instructions. Key building
blocks:
- `Instruction`: enum of all mnemonics (includes 65C02-specific ones: `RMB`,
  `SMB`, `BBR`, `BBS`, `WAI`, `STP`, `STZ`, `BRA`, `PHX`/`PHY`/`PLX`/`PLY`,
  `TRB`, `TSB`).
- `AddressingMode`: enum of addressing modes, several split into RMW/plain
  variants (e.g. `ABSOLUTE` vs `ABSOLUTE_RMW`) because read-modify-write
  addressing takes a different cycle sequence than a read.
- `instructions[]` / `addressingModes[]`: 256-entry opcode tables indexed by
  opcode byte, laid out as a 16x16 grid in source to mirror the standard
  65C02 opcode matrix — when adding/checking an opcode, find it in this grid
  by high/low nibble.
- `Cycle`: a record describing one machine cycle's bus behavior (`vpb`,
  `mlb`, `sync`, which `Address`/`Data` enum drives the bus, `rwb`).
- `Address` / `Data`: enums naming the internal CPU registers/latches that
  can drive the address/data bus during a given cycle.

`Clock` runs the clock signal on its own thread (`start()`/`stop()`) for
free-running execution, or can be single-stepped with `cycle()` — tests use
single-stepping exclusively, never the threaded runner.

### Expression language (`org.joev.ya6s.expression`)

Small expression tree (`Constant`, `Register`, `ArithmeticExpression`,
`LogicExpression`, `NotExpression`, `RelationalExpression`) used to evaluate
`break when {expression}` conditions against CPU registers (PC, A, X, Y, C,
N, Z, V, I, D).

### Monitor / REPL (`org.joev.ya6s.monitor`)

`MonitorParser`/`Tokenizer` parse monitor command lines into `Command`
objects (`AttachCommand`, `ReadCommand`, `WriteCommand`, `LoadCommand`,
`StepCommand`, `ContinueCommand`, `ResetCommand`, `DisassembleCommand`,
`Breakpoint*Command`, `Profile*Command`, etc.), each with an `execute(Monitor)`
method. `Monitor` holds the REPL loop state (backplane, clock, cpu, a JLine
`Terminal`/`LineReader`) and drives it. Config files passed on the command
line (`Main.java`) are just newline-separated monitor commands parsed and
executed the same way as interactive input. New monitor commands: add a
`Command` implementation and wire it into the grammar/`MonitorParser`.

Terminal I/O uses [JLine 4.x](https://github.com/jline/jline3) (`org.jline:jline-reader`/
`jline-terminal`/`jline-terminal-jni`, required as real JPMS modules in
`module-info.java` — JLine's 3.x line only ships automatic modules). `Main`
builds the system `Terminal` and owns its lifecycle (closed via a shutdown
hook calling `Monitor.close()`); `Monitor` builds its `LineReader` from it,
with command history persisted at `$XDG_STATE_HOME/ya6s/history` (default
`$HOME/.local/state/ya6s/history`). `Monitor.run()`'s command-phase prompt
uses `LineReader.readLine()` (catching `UserInterruptException`/
`EndOfFileException` for Ctrl-C/Ctrl-D); the run-phase loop (after `cont`)
puts the terminal in `enterRawMode()` and polls
`terminal.reader().read(timeout)` (a `NonBlockingReader`) so keystrokes typed
while the simulated program runs are forwarded live to the simulated UART,
while Ctrl-E is intercepted to pause execution.

## Testing conventions

- Tests are organized by addressing mode, one class per mode (e.g.
  `AbsoluteTests`, `ZeroPageIndirectTests`, `RelativeTests`, `StackTests`,
  `ImmediateTests`, `ImpliedTests`), each a JUnit 5 `@ParameterizedTest` over
  a `Stream<Parameters>` of hand-written test cases (opcode hex, expected
  cycle count, register/flag assertions).
- `Parameters` bundles a name, a raw hex program string (assembled by hand,
  comments after `;`), an expected cycle count, and a varargs list of
  `Consumer<W65C02S>` assertions.
- `TestUtils.load(...)` clocks raw hex bytes directly into memory at a given
  address (bypassing the CPU, via `rdy=false`); `TestUtils.executeTest(...)`
  loads the program at `$0200`, sets the reset vector to `$0200`, resets, and
  runs until `STP` (`DB`) or `maxCycles`, then asserts the cycle count exactly
  and runs the assertions. Programs must end in `DB` (STP) so the run loop
  terminates — cycle counts in `Parameters` are exclusive of the reset (7
  cycles) and STP (1 cycle) overhead, which `executeTest` adds automatically.
- `Assertions` has flag/register helpers (`assertNegative`, `assertZero`,
  `assertCarry`, `assertA(v)`, `assertMemory(backplane, addr, v)`, etc.) built
  by reading `cpu.p()`/`cpu.a()`/etc. directly, or by clocking a read cycle
  onto the bus for memory assertions.
- When adding a new opcode/addressing-mode test, follow the existing sibling
  class's pattern (`@BeforeEach` backplane/cpu/SRAM setup, `params(name,
  program, cycles, asserts...)` in the `tests()` stream) rather than
  introducing a new test harness.

## Examples

`examples/` holds sample programs (`hello`, `echo`, `counter-poll`,
`counter-interrupt`) each with a `.s` assembly source, `ld65.config` linker
config (cc65 toolchain), `build.sh`, and a `ya6s.config` monitor script that
attaches devices and loads the built ROM. `examples/6502_functional_test/`
holds config files for running Klaus Dormann's functional test binaries
(fetched separately, not checked in).
