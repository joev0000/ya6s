package joev.ya6s.monitor;

import joev.ya6s.Backplane;
import joev.ya6s.W65C02;
import joev.ya6s.signals.Signal;
import java.io.InputStream;

/**
 * A monitor for a system with a CPU and Backplane.
 */
public class Monitor {
  private final MonitorParser parser;
  private final Backplane backplane;
  private final W65C02 cpu;
  private final InputStream in;

  /**
   * Create a new Monitor.
   *
   * @param backplane the Backplane of the system
   * @param cpu the CPU of the system.
   * @param in the input stream of the user commands.
   */
  public Monitor(Backplane backplane, W65C02 cpu, InputStream in) {
    this.backplane = backplane;
    this.cpu = cpu;
    this.in = in;
    parser = new MonitorParser(in);
  }

  /**
   * Run the monitor loop.  Never exits.
   */
  public void run() {
    Signal clock = backplane.clock();
    Command command = null;
    while(true) {
      try {
        System.out.format(">>> ");
        command = parser.command();
        while(!command.equals(ContinueCommand.instance())) {
          command.execute(backplane, cpu);
          System.out.format(">>> ");
          command = parser.command();
        }
        while(System.in.available() == 0) {
          clock.value(true);
          clock.value(false);
        }
        System.in.read();
        while(backplane.sync().value() == false) {
          clock.value(true);
          clock.value(false);
        }
        System.out.println("Paused.");
        System.out.format("PC: $%04X,  A: $%02X,  X: $%02X,  Y: $%02X,  S: $%02X,  P: $%02X (%s)%n", cpu.pc(), cpu.a(), cpu.x(), cpu.y(), cpu.s(), cpu.p(), cpu.status());
      }
      catch (Exception e) {
        System.out.format("ERROR: %s%n", e.getMessage());
        parser.ReInit(in);
      }
    }
  }
}
