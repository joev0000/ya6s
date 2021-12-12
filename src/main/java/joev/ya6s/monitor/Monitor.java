package joev.ya6s.monitor;

import joev.ya6s.Backplane;
import joev.ya6s.W65C02S;
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
  private final W65C02S cpu;
  private final Smartline sl;
  private final PrintStream out;
  private final OutputStream console;
  private final List<Predicate<W65C02S>> breakpoints = new ArrayList<>();

  private long profile[] = new long[65536];
  private short profilePC;
  private final Signal.Listener syncFn = this::sync;

  public static InputStream ttyIn;
  public static OutputStream ttyOut;

  /**
   * Create a new Monitor.
   *
   * @param backplane the Backplane of the system
   * @param cpu the CPU of the system.
   * @param in the input stream of the user commands.
   */
  public Monitor(Backplane backplane, W65C02S cpu, InputStream in, OutputStream out, OutputStream console) {
    this.backplane = backplane;
    this.cpu = cpu;
    this.out = (out instanceof PrintStream) ? (PrintStream)out : new PrintStream(out);
    this.console = console;
    sl = new Smartline(in, out);

    backplane.sync().register(syncFn);
  }

  /**
   * Update the profile program counter on instruction load cycles.
   *
   * @param eventType the edge type of the sync signal.
   */
  private void sync(Signal.EventType eventType) {
    if(eventType == Signal.EventType.POSITIVE_EDGE) {
      profilePC = cpu.pc();
    }
  }

  /**
   * Add a breakpoint, which is a Predicate tested against a CPU.
   *
   * @param predicate the Predicate to test.
   */
  public void addBreakpoint(Predicate<W65C02S> predicate) {
    System.out.format("Adding breakpoint: %s%n", predicate);
    breakpoints.add(predicate);
  }

  /**
   * List the breakpoints.
   *
   * @return the list of breakpoints.
   */
  public List<Predicate<W65C02S>> listBreakpoints() {
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
  public W65C02S cpu() {
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
   * Get the profiling data.
   *
   * @return the profile cycle count metric array.
   */
  public long[] profile() {
    return profile;
  }

  /**
   * Update the profiling data with the current profile PC.
   */
  public void updateProfile() {
    profile[profilePC & 0xFFFF]++;
  }

  /**
   * Reset the profiling data.
   */
  public void profileReset() {
    for(int i = 0; i < profile.length; i++) {
      profile[i] = 0;
    }
  }

  /**
   * Run the monitor loop.  Never exits.
   */
  public void run() {
    MonitorParser parser;
    Signal clock = backplane.clock();
    Signal sync = backplane.sync();
    Command command;
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

        Predicate<W65C02S> breakpoint = null;
        // At this point, the Continue command was used.
        out.println("(Ctrl-E to pause.)");
        updateProfile();
        clock.value(true);
        clock.value(false);

        // Cycle the clock until either the CPU is stopped, or if ^E is
        // entered in the console.
        while(!cpu.stopped() && (c = sl.read()) != 0x05) { // ^E
          // if sync, check breakpoint
          if(sync.value()) {
            for(Predicate<W65C02S> predicate: breakpoints) {
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
          updateProfile();
          clock.value(true);
          clock.value(false);
        }

        // The processor is either stopped or paused. Cycle the clock
        // until the next SYNC pulse- to make sure the entire instruction
        // has been executed.
        while(!sync.value()) {
          updateProfile();
          clock.value(true);
          clock.value(false);
        }
        if(breakpoint != null) {
          out.format("Breakpoint: %s%n", breakpoint);
        }
        else {
          out.println(cpu.stopped() ? "Stopped." : "Paused.");
        }
        out.format("PC: $%04X,  A: $%02X,  X: $%02X,  Y: $%02X,  S: $%02X,  P: $%02X (%s) cycles: %d%n", (short)(cpu.pc() - 1), cpu.a(), cpu.x(), cpu.y(), cpu.s(), cpu.p(), cpu.status(), cpu.cycleCount());
      }
      catch (Exception e) {
        out.format("ERROR: %s%n", e.getMessage());
      }
    }
  }
}
