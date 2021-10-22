package joev.ya6s;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

import joev.ya6s.monitor.Command;
import joev.ya6s.monitor.Monitor;
import joev.ya6s.monitor.MonitorParser;
import joev.ya6s.monitor.ParseException;
import joev.ya6s.signals.Signal;
import joev.ya6s.signals.Bus;

public class Main {

  public static void load(Backplane backplane, int start, int... bytes) {
    Signal clock = backplane.clock();
    Bus address = backplane.address();
    Bus data = backplane.data();
    clock.value(false);
    for(int i = 0; i < bytes.length; i++) {
      clock.value(false);
      address.value((short)(start + i));
      data.value(bytes[i] & 0xFF);
      clock.value(true);
    }
  }

  public static void main(String[] args) throws Exception {
    /*
    final Backplane backplane = new Backplane();

    final SRAM ram = new SRAM(backplane);
    final W65C02S cpu = new W65C02S(backplane);

    Bus address = backplane.address();
    Bus data = backplane.data();
    Signal clock = backplane.clock();
    backplane.be().value(true);
    backplane.sync().register(et -> {
      if(et == Signal.EventType.POSITIVE_EDGE) {
        System.out.format("sync: PC: %04X  A: %02X  X: %02X  Y: %02X  S: %02X  P: %02X (%s)%n", cpu.pc(), cpu.a(), cpu.x(), cpu.y(), cpu.s(), cpu.p(), cpu.status());
      }
    });

    short start = 0x200;
    cpu.rdy().value(false);
    //load(backplane, start, 0xA9, 0x30, 0x85, 0x20, 0xA9, 0x12, 0x85, 0x21, 0xA0, 0x04, 0xB1, 0x20);
    //load(backplane, start, 0xA9, 0x01, 0xD0, 0x04, 0xA9, 0x42, 0xDB, 0x00, 0xA9, 0x23, 0xDB);
    load(backplane, start, 0xA9, 0xAA, 0x85, 0x20, 0xC7, 0x20, 0xA6, 0x20, 0xDB);

    load(backplane, 0xFFFC, start & 0xFF, start >> 8);
    cpu.rdy().value(true);

    cpu.resb().value(false);
    clock.value(false); clock.value(true);
    clock.value(false); clock.value(true);
    cpu.resb().value(true);
    for(int i = 0; (i < 50 && !cpu.stopped()); i++) {
      clock.value(false);
      clock.value(true);
    }
*/

    final Backplane backplane = new Backplane();
    PipedInputStream uartIn = new PipedInputStream();
    PipedOutputStream toUartIn = new PipedOutputStream(uartIn);
    Monitor.ttyIn = uartIn;
    Monitor.ttyOut = System.out;

    final W65C02S cpu = new W65C02S(backplane);
    final Signal resb = cpu.resb();
    resb.value(true);
    backplane.be().value(true);
    Monitor monitor = new Monitor(backplane, cpu, System.in, System.out, toUartIn);
    if(args.length > 0) {
      Files.lines(Path.of(args[0]))
        .map(l -> l.trim())
        .filter(l -> l.length() > 0)
        .forEach(l -> {
          try {
            MonitorParser parser = new MonitorParser(new StringReader(l));
            Command command = parser.command();
            command.execute(monitor);
          }
          catch (ParseException pe) {
            System.err.format("Error parsing \"%s\": %s%n", l, pe.getMessage());
          }
        });
    }
    monitor.run();
  }

  /**
   * This clas cannot be instantiated.
   */
  private Main() {
    // private default constructor
  }
}
