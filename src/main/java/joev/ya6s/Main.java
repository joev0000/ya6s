package joev.ya6s;

public class Main {
  public static void main(String[] args) throws Exception {
    W65C02 cpu = new W65C02();

    byte[] m = cpu.memory();
    byte[] prg = new byte[] {
      /*
      (byte)0xA9, (byte)0x11,             // LDA #$11
      (byte)0xA2, (byte)0x0F,             // LDX #$0F
      (byte)0x18,                         // CLC
      (byte)0x7D, (byte)0xF0, (byte)0x10, // ADC $10F0,X
      (byte)0xDB                          // STP
      */
      (byte)0xA9, (byte)0x23, (byte)0xDB
    };
    System.arraycopy(prg, 0, m, 0x200, prg.length);

    m[0x10F0] = (byte)0xF3;
    m[0x10FF] = (byte)0x22;

    m[0xFFFC] = (byte)0x00;
    m[0xFFFD] = (byte)0x02;

    cpu.reset();
    while(!cpu.stopped()) {
      cpu.tick();
    }
  }
}

