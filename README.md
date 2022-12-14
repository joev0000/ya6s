# Yet Another 6502 Simulator

Yet Another 6502 Simulator (ya6s) is yet another 6502 simulator.  This one
uses a circuit simulation model that mimics the behavior of a WDC 65C02
microprocessor at the cycle level. It is built upon a simple digital signal
model, which can be used to simulate other devices attached to the busses
and processor signals via virtual backplane. An out-of-band monitor is also
included that can be used to load data from files on to the bus, as well
as adding breakpoints and inspection commands to assist in debugging.

This simulator is intended to be a software-based prototyping environment
for a 65C02-based computer. It is designed to allow a system designer to
include simulated hardware peripherals by monitoring the signals generated
by the processor, and potentially other peripherals.

ya6s successfully passes Klaus Dormann's 6502 and 65C02
[functional tests](https://github.com/Klaus2m5/6502_65C02_functional_tests)

## Example

```
$ ya6s hello.config
A: $00,  X: $00,  Y: $00,  S: $00,  P: $20 (nv1bdizc) cycles: 0
0000:  00 00     BRK #$00
>>> r f800 f83f
       0  1  2  3  4  5  6  7   8  9  A  B  C  D  E  F   01234567 89ABCDEF
F800: A9 83 8D 03 F0 A9 78 8D  00 F0 9C 01 F0 A9 03 8D  |©...ð©x. .ð..ð©..|
F810: 03 F0 A9 20 A2 00 BC 28  F8 F0 0B 2C 05 F0 F0 FB  |.ð© ¢.¼( øð.,.ððû|
F820: 8C 00 F0 E8 80 F0 DB 40  48 65 6C 6C 6F 2C 20 77  |..ðè.ðÛ@ Hello, w|
F830: 6F 72 6C 64 21 0D 0A 00  00 00 00 00 00 00 00 00  |orld!... ........|
A: $00,  X: $00,  Y: $00,  S: $00,  P: $20 (nv1bdizc) cycles: 0
0000:  00 00     BRK #$00
>>> cont
(Ctrl-E to pause.)
Hello, world!
Stopped.
A: $20,  X: $0F,  Y: $00,  S: $FD,  P: $26 (nv1bdIZc) cycles: 27359
F827:  40        RTI
>>> exit
$
```

## Building

### Requirements

* [JDK 17](https://openjdk.java.net/projects/jdk/17/)
* [Gradle 7.4.2](https://gradle.org/releases/)

### Steps

```
gradlew build
```

The `build/distributions` directory will contain `ya6s.tar` and `ya6s.zip`.

## Usage

Note: Either a JDK 17+ distribution directory must appear in `JAVA_HOME`, or
a JDK 17+ `java` command must appear in the `PATH`.

Unpack the `ya6s.tar` or `ya6s.zip` files into a directory, and run `bin/ya6s`
from that directory. This will enter the monitor, with a system with no
devices, not even memory.

Monitor commands can be used to attach devices, and write data to devices
from a file.  ya6s includes four devices: RAM, ROM, a 16550-based UART, and
a counter device, which can be used to generate periodic interrupts. The
`attach` command also takes device-specific configuration options.

For example, the following command attaches a 32 kilobyte RAM device that
starts at location 0 in the memory map:
```
>>> attach joev.ya6s.SRAM base=0000 size=8000
```

With this, the monitor can be used to read and write to RAM:
```
>>> write 1000 41 42 43 44
>>> read 1000 100f
       0  1  2  3  4  5  6  7   8  9  A  B  C  D  E  F   01234567 89ABCDEF
1000: 41 42 43 44 00 00 00 00  00 00 00 00 00 00 00 00  |ABCD.... ........|
>>> load 2000 hello.txt
>>> read 2000 200f
       0  1  2  3  4  5  6  7   8  9  A  B  C  D  E  F   01234567 89ABCDEF
2000: 48 65 6C 6C 6F 2C 20 77  6F 72 6C 64 21 0A 00 00  |Hello, w orld!...|
```

A pre-loaded ROM device can be attached. This one is 2KB, starting at $F800.
```
>>> attach joev.ya6s.ROM  base=F800 size=0800 file=rom.bin
>>> read f800 f83f
       0  1  2  3  4  5  6  7   8  9  A  B  C  D  E  F   01234567 89ABCDEF
F800: A9 83 8D 03 F0 A9 78 8D  00 F0 9C 01 F0 A9 03 8D  |©...ð©x. .ð..ð©..|
F810: 03 F0 A9 20 A2 00 BC 28  F8 F0 0B 2C 05 F0 F0 FB  |.ð© ¢.¼( øð.,.ððû|
F820: 8C 00 F0 E8 80 F0 DB 40  48 65 6C 6C 6F 2C 20 77  |..ðè.ðÛ@ Hello, w|
F830: 6F 72 6C 64 21 0D 0A 00  00 00 00 00 00 00 00 00  |orld!... ........|
>>> disassemble f800
F800:  A9 83     LDA #$83
F802:  8D 03 F0  STA $F003
F805:  A9 78     LDA #$78
F807:  8D 00 F0  STA $F000
F80A:  9C 01 F0  STZ $F001
```

This program expects a 16550 UART to be available at address $F000
```
>>> attach joev.ya6s.UART base=F000
```

When a 6502 microprocessor is reset, it will read the two byte value at $FFFC
and $FFFD and jump to that address. ya6s works the same way; there must be
a device mapped to those addresses which contains the address of the start
of the program. In the example above, the `rom.bin` includes these values
at $FFFC and $FFFD, which point to $F800 as the starting address:
```
>>> read fffc fffd
       0  1  2  3  4  5  6  7   8  9  A  B  C  D  E  F   01234567 89ABCDEF
FFF0: 00 00 00 00 00 00 00 00  00 00 27 F8 00 F8 27 F8  |........ ..'ø.ø'ø|
```

The `reset` command simulates pulling the 6502's /RES low, which triggers the
processor reset sequence. The `cont` command starts the system clock, which
cycles the processor until the processor is stopped (via the 65C02 STP
instruction), or when the user types Ctrl-E.
```
>>> reset
>>> cont
(Ctrl-E to pause.)
Hello, world!
Stopped.
A: $20,  X: $0F,  Y: $00,  S: $FD,  P: $66 (nV1bdIZc) cycles: 30477
```

If the Ctrl-E command is used, the program is paused, and can be resumed
with the `cont` command.

These commands can be stored in a configuration file, which can be specified
on the command line at startup:

```
$ ls
hello.config  rom.bin
$ cat hello.config
attach joev.ya6s.SRAM base=0000 size=8000
attach joev.ya6s.UART base=F000
attach joev.ya6s.ROM  base=F800 size=0800 file=rom.bin
$ ya6s hello.config
A: $00,  X: $00,  Y: $00,  S: $00,  P: $20 (nv1bdizc) cycles: 0
0000:  00 00     BRK #$00
>>> reset
A: $00,  X: $00,  Y: $00,  S: $FF,  P: $20 (nv1bdizc) cycles: 0
0100:  00 00     BRK #$00
>>> cont
(Ctrl-E to pause.)
Hello, world!
Stopped.
A: $20,  X: $0F,  Y: $00,  S: $FD,  P: $66 (nV1bdIZc) cycles: 26733
F827:  40        RTI
>>> exit
$
```

# Monitor Commands

## attach

    attach {type} ({name}={value})*

Attach a device to the simulated computer.  The type is a name of a class
in the `CLASSPATH` that has a constructor that takes the arguments
`(Backplane, Map<String, String>)`.  See the "Custom Devices" section for
more information about how to build a custom device.  The included devices
are

* joev.ya6s.SRAM: A RAM device.  The options are `base`, which is the first
address (in hex) within the memory map this device listens on, and `size` the
number of bytes (in hex) of the RAM device.
* joev.ya6s.ROM: A ROM device. The options are `base`, which is the first
address (in hex) within the memory map this devices listens on, `size` which
is the number of bytes (in hex) of the ROM device, and `file` which is the
name of a file that is loaded into the ROM.
* joev.ya6s.UART: A UART device based on the widely-used 16550 UART IC. The
option `base` tells the device the address (in hex) of the first register.
* joev.ya6s.Counter: An example device that counts the cycles of the computer.
A program can set a value as 24 bit number, the base to count down, and the
device can be configured to generate an interrupt when the counter reaches
zero. The first three bytes are the counter value, little-endian, the fourth
byte is the control and status register, where bit 0 is the counter enable
flag, bit 2 is the interrupt enable flag, bit 6 indicates if the counter
is zero. Bit 6 is used since the 6502 BIT instruction sets the overflow flag
based on bit 6. This device has a `base` option which is the address (in hex)
of the first register.

## breakpoint

    break list
    break remove {index}
    break at {address}
    break when {expression}

Manage breakpoints.  `break at` enables a breakpoint at the specified
address. `break when` enables a breakpoint when the expression evaluates to
true. `break list` lists the enabled breakpoints, `break remove` removes a
breakpoint.  Breakpoint expressions are of the form `{constant|register} {op}
{constant|register}`.  The supported registers are PC, A, X, Y, C, N, Z, V, I,
and D. The supported operations are =, >, <, !=, >=, and <=.

## cont

    cont

The cont command resumes the program when the program as stopped or paused
using Ctrl-E.

## disassemble

    disassemble ({address} ({count})?)?

Disassembles the program instructions. If no address is given, the program
starting at PC is disassembled.  If no count is given, the next 5 instructions
are disassembled.

## exit

    exit

Exits ya6s.

## load

    load {start} {path}

Loads the bytes from the file at the given path, to the start address (in hex).
This command disables the CPU, and cycles the clock while sequentially
placing the bytes on the data bus while incrementing the address bus.
Typically this will be used with the address a RAM-like device, but this
command will write to whatever device appears at that address.

## profile

    profile on
    profile off
    profile show ({maxLines})?
    profile reset

Track the addresses which are being executed.  `profile on` enables tracking
each address where an instruction is read from.  `profile off` disables this
tracking.  `profile show` shows which addresses have been executed, sorted by
the number of times the address was executed.  `profile reset` resets the
profile counter.

## read

    read {start} ({end})?

Reads the data from the memory map from the start address, to the end address.
If no end address is specified, one page (256 bytes) is read.

## reset

    reset

Toggles the reset pin on the processor, initiating the reset procedure. This
reads the contents of memory locations $FFFC and $FFFD, and jumps to the
location contained in those memory locations.

## step

    step

Executes a single instruction.

## write

    write {start} ({value})*

Writes the values to memory starting at the given start address.

# Custom Devices

ya6s is based on a simple digital circuit model. `Signal`s hold a boolean
value, and interested code can subscribe to changes of the `Signal`, which
are notified if the change was a positive edge (false to true), or a negative
edge (true to false). A `Bus` can hold a multiple bit value.

A simulated computer is based around a `Backplane`, which holds the signals
and busses that are commonly found in 6502-based computers. Typically, these
mirror the pins on the 6502: a 16 bit address bus, an 8 bit data bus, signals
for read/write, interrupts, sync, and clock.

The W65C02S class uses `Signal`s and `Bus`ses that closely match those of a
real WDC 65C02S processor, and is connected to a `Backplane`. Likewise, the
`SRAM`, `ROM`, `UART`, and `Counter` classes connect to the `Backplane` and
subscribe to the signal change events, and update the signals accordingly.

Devices do not interact with each other directly, only through the
signals on the backplane. Typically, a device will subscribe to the `clk`
signal on the backplane, and upon a negative edge transition, check the
address bus to determine if the device needs to take any action. If the `rwb`
signal is true, the device should write a value to the data bus, and if it
is false, it should read the value from the data bus.

Custom devices are no different than the included devices, and are built to
use the same `Backplane`. The ya6s simulator can dynamically attach these to
the backplane using the monitor's `attach` command.

A custom device must have a public constructor that takes the arguments
`(joev.ya6s.Backplane backplane, Map<String, String> options)`. It does not
need to extend any other class or implement any interface. The constructor
should register itself with the appropriate `Signal`s of the backplane, in
particular, the `clk` clock signal.  Typically, a device will have a `tick`
method, which is registered to listen to changes of the `clk` clock signal.
The `tick` method will check the address bus and determine if the address
corresponds to the device. If so, the `rwb` (read/not-write) flag is checked,
and the data bus is either written to or read from, and the appropriate
action taken.

# Future Enhancements

* Package Java runtime using `jlink`
* Add terminal/monitor support for Windows and MacOS.
* Add TCP support to UART.
* Address remaining cycle timing errors.
