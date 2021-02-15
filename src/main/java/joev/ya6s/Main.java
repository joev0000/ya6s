package joev.ya6s;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import joev.ya6s.signals.Bus;
import joev.ya6s.signals.Signal;

public class Main {
  public static void main(String[] args) throws Exception {
    final Backplane backplane = new Backplane();
    final SRAM ram = new SRAM(backplane);
    final Signal sync = backplane.sync();
    final Signal clock = backplane.clock();

    load(backplane, 0xFFFC, "00 02");

     /* A9 00    ;     LDA #$00
      * 8D 02 10 ;     STA $1002
      * 8D 01 10 ; L1: STA $1001
      * 8D 00 10 ; L2: STA $1000
      * CE 00 10 ; L3: DEC $10000
      * D0 FB    ;     BNE L3
      * CE 01 10 ;     DEC $1001
      * D0 F3    ;     BNE L2
      * CE 02 10 ;     DEC $1002
      * D0 EB    ;     BNE L1
      * DB       ;     STP
      */
    load(backplane, 0x200, "A9 00 8D 02 10 8D 01 10 8D 00 10 CE 00 10 D0 FB CE 01 10 D0 F3 CE 02 10 D0 EB DB");

    final W65C02 cpu = new W65C02(backplane);
    final Signal resb = cpu.resb();
    resb.value(true);
    /*
    sync.register(et -> {
      if(et == Signal.EventType.POSITIVE_EDGE) {
        System.out.format("PC: $%04X, A: $%02X, X: $%02X, Y: $%02X, S: $%02X, P: $%02X (%s)%n", cpu.pc(), cpu.a(), cpu.x(), cpu.y(), cpu.s(), cpu.p(), cpu.status());
      }
    });
    */

    int c = 0;
    Instant instant = Instant.now();
    while(!cpu.stopped()) {
      clock.value(true);
      clock.value(false);
      c++;
    }
    long t = instant.until(Instant.now(), ChronoUnit.MICROS);
    System.out.format("Finished after %d cycles in %dus.  %f cycles per second.%n", c, t, (double)c * 1000000d / (double)t);
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

