package joev.ya6s.monitor;

import joev.ya6s.Backplane;
import joev.ya6s.Clock;
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
  private final Clock clock;
  private final W65C02S cpu;
  private final Smartline sl;
  private final PrintStream out;
  private final OutputStream console;
  private final List<Predicate<W65C02S>> breakpoints = new ArrayList<>();
  private Predicate<W65C02S> breakpoint = null;

  private long profile[] = new long[65536];
  private final Signal.Listener syncFn = this::sync;
  private final Signal.Listener breakpointSync = this::breakpointSync;

  public static InputStream ttyIn;
  public static OutputStream ttyOut;

  /**
   * Create a new Monitor.
   *
   * @param backplane the Backplane of the system
   * @param cpu the CPU of the system.
   * @param in the input stream of the user commands.
   */
  public Monitor(Backplane backplane, Clock clock, W65C02S cpu, InputStream in, OutputStream out, OutputStream console) {
    this.backplane = backplane;
    this.clock = clock;
    this.cpu = cpu;
    this.out = (out instanceof PrintStream) ? (PrintStream)out : new PrintStream(out);
    this.console = console;
    sl = new Smartline(in, out);

    backplane.sync().register(syncFn);
  }

  /**
   * Get the clock.
   *
   * @return the clock.
   */
  public Clock clock() {
    return clock;
  }

  /**
   * Update the profile program counter on instruction load cycles.
   *
   * @param eventType the edge type of the sync signal.
   */
  private void sync(Signal.EventType eventType) {
    if(eventType == Signal.EventType.POSITIVE_EDGE) {
      updateProfile((short)backplane.address().value());
    }
  }

  /**
   * Add a breakpoint, which is a Predicate tested against a CPU.
   *
   * @param predicate the Predicate to test.
   */
  public void addBreakpoint(Predicate<W65C02S> predicate) {
    System.out.format("Adding breakpoint: %s%n", predicate);
    if(breakpoints.size() == 0) {
      backplane.sync().register(breakpointSync);
    }
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
    if(breakpoints.size() == 0) {
      backplane.sync().unregister(breakpointSync);
    }
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
  public void updateProfile(short pc) {
    profile[pc & 0xFFFF]++;
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
   * Stop the clock.  This is called when rdy changes.
   *
   * @param eventType the type of the signal event.
   */
  private void stopClock(Signal.EventType eventType) {
    if(eventType == Signal.EventType.NEGATIVE_EDGE) {
      clock.stop();
    }
  }

  /**
   * Check the breakpoints, and stop the clock if one is hit.
   * Called when a sync occurs.
   *
   * @param eventType the type of the signal event.
   */
  private void breakpointSync(Signal.EventType eventType) {
    if(eventType == Signal.EventType.POSITIVE_EDGE) {
      breakpoint = null;
      for(Predicate<W65C02S> predicate: breakpoints) {
        if(predicate.test(cpu)) {
          breakpoint = predicate;
          clock.stop();
        }
      }
    }
  }

  /**
   * Run the monitor loop.  Never exits.
   */
  public void run() {
    MonitorParser parser;
    Signal sync = backplane.sync();
    Signal rdy = backplane.rdy();
    rdy.register(this::stopClock);
    Command command = NoopCommand.instance();
    int c;
    while(true) {
      try {
        // Run commands one at a time until a Continue command is parsed.
        while(!command.equals(ContinueCommand.instance())) {
          command.execute(this);
          String string = sl.readLine(">>> ");
          parser = new MonitorParser(new StringReader(string));
          command = parser.command();
        }
        command = NoopCommand.instance();

        out.println("(Ctrl-E to pause.)");
        clock.start();
        while(clock.running()) {
          c = sl.read();
          if(c == 0x05) { // ^E
            clock.stop();
          }
          else if(c >= 0) {
            console.write(c);
          }
          Thread.sleep(100);
        }

        if(breakpoint != null) {
          out.format("Breakpoint: %s%n", breakpoint);
        }
        else {
          out.println(rdy.value() ? "Paused." : "Stopped.");
        }
        out.format("PC: $%04X,  A: $%02X,  X: $%02X,  Y: $%02X,  S: $%02X,  P: $%02X (%s) cycles: %d%n", (short)(cpu.pc() - 1), cpu.a(), cpu.x(), cpu.y(), cpu.s(), cpu.p(), cpu.status(), cpu.cycleCount());
      }
      catch (Exception e) {
        out.format("ERROR: %s%n", e.getMessage());
      }
    }
  }
}
