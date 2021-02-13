package joev.ya6s;

import joev.ya6s.signals.Bus;
import joev.ya6s.signals.Signal;

public class Main {
  public static void main(String[] args) throws Exception {
    /*
    final Bus address = new Bus("address", 16);
    final Bus data = new Bus("data", 8);
    final Signal rwb = new Signal("rwb");
    final Signal clock = new Signal("clock");
    final Signal sync = new Signal("sync");
    final Signal resb = new Signal("resb");
*/
    final Backplane backplane = new Backplane();
    final SRAM ram = new SRAM(backplane);
    final Signal sync = backplane.sync();
    final Signal clock = backplane.clock();

    /*
    clock.register(et -> {
      if(et == Signal.EventType.POSITIVE_EDGE) {
        System.out.format("+++ clock: positive %s %04X%n", rwb.value() ? "read " : "write", address.value());
      }
      else { System.out.println("--- clock: negative"); }
    });
    */
    load(backplane, 0x200, "A9 23 8D 00 10 A9 42 2A 2E 00 10 AD 00 10 DB");
    load(backplane, 0xFFFC, "00 02");
    //cpu.reset();
    System.out.println("LOAD COMPLETE");
    final W65C02 cpu = new W65C02(backplane);
    final Signal resb = cpu.resb();
    resb.value(true);
    sync.register(et -> {
      if(et == Signal.EventType.POSITIVE_EDGE) {
        System.out.format("PC: $%04X, A: $%02X, X: $%02X, Y: $%02X, S: $%02X, P: $%02X (%s)%n", cpu.pc(), cpu.a(), cpu.x(), cpu.y(), cpu.s(), cpu.p(), cpu.status());
      }
    });

    int c = 0;
    while(!cpu.stopped() && c++ <= 100) {
      clock.value(true);
      clock.value(false);
    }
  }

  public static final void load(Backplane backplane, int addr, String bytes) {
    Signal clock = backplane.clock();
    Bus address = backplane.address();
    Bus data = backplane.data();
    Signal rwb = backplane.rwb();
    String[] hex = bytes.split("[ \t\n\r]+");
    for(int i = 0; i < hex.length; i++) {
      clock.value(false);
      address.value(addr++);
      rwb.value(false);
      data.value(Integer.decode("0x" + hex[i]));
      clock.value(true);
    }
  }
}

