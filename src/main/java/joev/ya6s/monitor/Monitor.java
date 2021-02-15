package joev.ya6s.monitor;

import joev.ya6s.Backplane;
import joev.ya6s.W65C02;
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
    Command command = null;
    while(true) {
      System.out.format(">>> ");
      try {
        command = parser.command();
        System.out.format("command: %s%n", command);
        command.execute(backplane, cpu);
      }
      catch (Exception e) {
        System.out.format("ERROR: %s%n", e.getMessage());
        parser.ReInit(in);
      }
    }
  }
}
