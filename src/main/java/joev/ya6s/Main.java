package joev.ya6s;

import joev.ya6s.monitor.Monitor;
import joev.ya6s.signals.Signal;

public class Main {
  public static void main(String[] args) throws Exception {
    final Backplane backplane = new Backplane();
    new SRAM(backplane);

    final W65C02 cpu = new W65C02(backplane);
    final Signal resb = cpu.resb();
    resb.value(true);

    Monitor monitor = new Monitor(backplane, cpu, System.in, System.out);
    monitor.run();
  }
}
