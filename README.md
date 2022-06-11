# Yet Another 6502 Simulator

Yet Another 6502 Simulator (ya6s) is yet another 6502 simulator.  This one
uses a circuit simulation model that mimics the behavior of a WDC 65C02
microprocessor at the cycle level. It is built upon a simple digital signal
model, which can be used to simulate other devices attached to the busses
and processor signals via virtual backlplane. An out-of-band monitor is also
included that can be used to load data from files on to the bus, as well
as adding breakpoints and inspection commands to assist in debugging.

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

## Future Enhancements

* Package Java runtime using `jlink`
* Add terminal/monitor support for Windows and MacOS.
* Add TCP support to UART.
