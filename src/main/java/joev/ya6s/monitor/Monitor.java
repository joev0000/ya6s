package joev.ya6s.monitor;

import joev.ya6s.Backplane;
import joev.ya6s.W65C02;
import joev.ya6s.signals.Signal;
import joev.ya6s.smartline.Smartline;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

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
  private final List<Predicate<W65C02>> breakpoints = new ArrayList<>();

  public static InputStream ttyIn;
  public static OutputStream ttyOut;

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
   * Add a breakpoint, which is a Predicate tested against a CPU.
   *
   * @param predicate the Predicate to test.
   */
  public void addBreakpoint(Predicate<W65C02> predicate) {
    System.out.format("Adding breakpoint: %s%n", predicate);
    breakpoints.add(predicate);
  }

  /**
   * List the breakpoints.
   *
   * @return the list of breakpoints.
   */
  public List<Predicate<W65C02>> listBreakpoints() {
    return breakpoints;
  }

  /**
   * Remove a breakpoint.
   *
   * @param index the index within the list of breakpoints to remove.
   */
  public void removeBreakpoint(int index) {
    breakpoints.remove(index);
  }

  /**
   * Get the CPU this monitor is monitoring.
   *
   * @return the CPU.
   */
  public W65C02 cpu() {
    return cpu;
  }

  /**
   * Get the Backplane this monitor is monitoring.
   *
   * @return the Backplane.
   */
  public Backplane backplane() {
    return backplane;
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

        // Run commands one at a time until a Continue command is parsed.
        while(!command.equals(ContinueCommand.instance())) {
          command.execute(this);
          string = sl.readLine(">>> ");
          parser = new MonitorParser(new StringReader(string));
          command = parser.command();
        }

        Predicate<W65C02> breakpoint = null;
        // At this point, the Continue command was used.
        out.println("(Ctrl-E to pause.)");

        // Cycle the clock until either the CPU is stopped, or if ^E is
        // entered in the console.
        while(!cpu.stopped() && (c = sl.read()) != 0x05) { // ^E
          // if sync, check breakpoint
          if(backplane.sync().value()) {
            for(Predicate<W65C02> predicate: breakpoints) {
              if(predicate.test(cpu)) {
                breakpoint = predicate;
                break;
              }
            }
          }
          if(breakpoint != null) {
            break;
          }
          if(c >= 0) console.write(c);
          clock.value(true);
          clock.value(false);
        }

        // The processor is either stopped or paused. Cycle the clock
        // until the next SYNC pulse- to make sure the entire instruction
        // has been executed..
        while(backplane.sync().value() == false) {
          clock.value(true);
          clock.value(false);
        }
        if(breakpoint != null) {
          out.format("Breakpoint: %s%n", breakpoint);
        }
        else {
          out.println(cpu.stopped() ? "Stopped." : "Paused.");
        }
        out.format("PC: $%04X,  A: $%02X,  X: $%02X,  Y: $%02X,  S: $%02X,  P: $%02X (%s)%n", cpu.pc(), cpu.a(), cpu.x(), cpu.y(), cpu.s(), cpu.p(), cpu.status());
      }
      catch (Exception e) {
        out.format("ERROR: %s%n", e.getMessage());
      }
    }
  }
}
