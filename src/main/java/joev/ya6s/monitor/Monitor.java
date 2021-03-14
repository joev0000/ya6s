package joev.ya6s.monitor;

import joev.ya6s.Backplane;
import joev.ya6s.W65C02;
import joev.ya6s.signals.Signal;
import joev.ya6s.smartline.Smartline;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;

/**
 * A monitor for a system with a CPU and Backplane.
 */
public class Monitor {
  //private final MonitorParser parser;
  private final Backplane backplane;
  private final W65C02 cpu;
  private final Smartline sl;
  private final PrintStream out;
  private final OutputStream console;

  /**
   * Create a new Monitor.
   *
   * @param backplane the Backplane of the system
   * @param cpu the CPU of the system.
   * @param in the input stream of the user commands.
   */
  public Monitor(Backplane backplane, W65C02 cpu, InputStream in, OutputStream out, OutputStream console) {
    this.backplane = backplane;
    this.cpu = cpu;
    this.out = (out instanceof PrintStream) ? (PrintStream)out : new PrintStream(out);
    this.console = console;
    sl = new Smartline(in, out);
  }

  /**
   * Run the monitor loop.  Never exits.
   */
  public void run() {
    MonitorParser parser;
    Signal clock = backplane.clock();
    Command command = null;
    int c;
    while(true) {
      try {
        String string = sl.readLine(">>> ");
        parser = new MonitorParser(new StringReader(string));
        command = parser.command();
        while(!command.equals(ContinueCommand.instance())) {
          command.execute(backplane, cpu);
          string = sl.readLine(">>> ");
          parser = new MonitorParser(new StringReader(string));
          command = parser.command();
        }
        out.println("(Ctrl-E to pause.)");
        while((c = sl.read()) != 0x05) { // ^E
          if(c >= 0) console.write(c);
          clock.value(true);
          clock.value(false);
        }
        while(backplane.sync().value() == false) {
          clock.value(true);
          clock.value(false);
        }
        out.println("Paused.");
        out.format("PC: $%04X,  A: $%02X,  X: $%02X,  Y: $%02X,  S: $%02X,  P: $%02X (%s)%n", cpu.pc(), cpu.a(), cpu.x(), cpu.y(), cpu.s(), cpu.p(), cpu.status());
      }
      catch (Exception e) {
        out.format("ERROR: %s%n", e.getMessage());
      }
    }
  }
}
