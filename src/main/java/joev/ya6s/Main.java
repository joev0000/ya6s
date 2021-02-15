package joev.ya6s;

import joev.ya6s.monitor.Monitor;
import joev.ya6s.signals.Bus;
import joev.ya6s.signals.Signal;

public class Main {
  public static void main(String[] args) throws Exception {
    final Backplane backplane = new Backplane();
    final SRAM ram = new SRAM(backplane);
    final Signal sync = backplane.sync();
    final Signal clock = backplane.clock();


    final W65C02 cpu = new W65C02(backplane);
    final Signal resb = cpu.resb();
    resb.value(true);

    Monitor monitor = new Monitor(backplane, cpu, System.in);
    monitor.run();

  }
}

