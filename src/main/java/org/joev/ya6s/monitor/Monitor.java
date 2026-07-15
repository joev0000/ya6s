/* Copyright (C) 2021, 2022 Joseph Vigneau */

package org.joev.ya6s.monitor;

import org.joev.ya6s.AddressingMode;
import org.joev.ya6s.Backplane;
import org.joev.ya6s.Clock;
import org.joev.ya6s.Instruction;
import org.joev.ya6s.W65C02S;
import org.joev.ya6s.signals.Bus;
import org.joev.ya6s.signals.Signal;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.utils.NonBlockingReader;

/**
 * A monitor for a system with a CPU and Backplane.
 */
public class Monitor {
  //private final MonitorParser parser;
  private final Backplane backplane;
  private final Clock clock;
  private final W65C02S cpu;
  private final Terminal terminal;
  private final LineReader reader;
  private final PrintWriter out;
  private final OutputStream console;
  private final List<Predicate<W65C02S>> breakpoints = new ArrayList<>();
  private Predicate<W65C02S> breakpoint = null;

  private boolean profiling = false;
  private long profile[] = new long[65536];
  private final Signal.Listener profileSync = this::profileSync;
  private final Signal.Listener breakpointSync = this::breakpointSync;

  public static InputStream ttyIn;
  public static OutputStream ttyOut;

  /**
   * Create a new Monitor.
   *
   * @param backplane the Backplane of the system
   * @param cpu the CPU of the system.
   * @param terminal the Terminal used for the monitor prompt and console passthrough.
   * @param console the OutputStream that keystrokes are forwarded to while the clock is running.
   */
  public Monitor(Backplane backplane, Clock clock, W65C02S cpu, Terminal terminal, OutputStream console) {
    this.backplane = backplane;
    this.clock = clock;
    this.cpu = cpu;
    this.terminal = terminal;
    this.out = terminal.writer();
    this.console = console;
    this.reader = LineReaderBuilder.builder()
        .terminal(terminal)
        .variable(LineReader.HISTORY_FILE, historyFile())
        .build();
  }

  /**
   * Determine the XDG-compliant location of the command history file,
   * honoring $XDG_STATE_HOME if set, defaulting to $HOME/.local/state otherwise.
   *
   * @return the path to the history file.
   */
  private static Path historyFile() {
    String xdgStateHome = System.getenv("XDG_STATE_HOME");
    Path stateHome = (xdgStateHome != null && !xdgStateHome.isBlank())
        ? Path.of(xdgStateHome)
        : Path.of(System.getProperty("user.home"), ".local", "state");
    return stateHome.resolve("ya6s").resolve("history");
  }

  /**
   * Save history and close the underlying Terminal.
   *
   * @throws IOException if the Terminal cannot be closed.
   */
  public void close() throws IOException {
    reader.getHistory().save();
    terminal.close();
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
    return Collections.unmodifiableList(breakpoints);
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
   * Enable or disable the profiling feature.
   *
   * @param enabled true enables profiling, false disables profiling.
   */
  public void profiling(boolean enabled) {
    if(enabled && !profiling) {
      backplane.sync().register(profileSync);
    }
    else if(profiling && !enabled) {
      backplane.sync().unregister(profileSync);
    }
    profiling = enabled;
  }

  /**
   * Update the profiling data with the current profile PC.
   *
   * @param pc the address to increment
   */
  public void updateProfile(short pc) {
    if(profiling) {
      profile[pc & 0xFFFF]++;
    }
  }

  /**
   * Update the profile program counter on instruction load cycles.
   *
   * @param eventType the edge type of the sync signal.
   */
  private void profileSync(Signal.EventType eventType) {
    if(profiling && eventType == Signal.EventType.POSITIVE_EDGE) {
      updateProfile((short)backplane.address().value());
    }
  }

  /**
   * Get the profiling data.
   *
   * @return the profile cycle count metric array.
   */
  public long[] profile() {
    return profile.clone();
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
   * Disassemble instructions.
   *
   * @param address the address to start the disassembly from.
   * @count the number of instructions to disassemble.
   * @return the disassembly of the instruction(s)
   */
  public String disassemble(short address, int count) {
    StringBuilder result = new StringBuilder();
    Bus addressBus = backplane.address();
    Bus dataBus = backplane.data();
    Signal clk = backplane.clock();
    Signal rdy = backplane.rdy();
    boolean rdyOld = rdy.value();
    short addressOld = (short)addressBus.value();
    byte dataOld = (byte)dataBus.value();
    boolean clockWasRunning = clock.running();
    rdy.value(false);
    byte[] insrBytes = new byte[3];

    // -1 means the current address, since it never makes sense to
    // disassemble $FFFF
    if(address == -1) {
      address = addressOld;
    }
    for(;count != 0; count--) {
      result.append(String.format("%04X: ", address));
      addressBus.value(address++);
      clk.value(false);
      clk.value(true);
      Instruction insr = W65C02S.instructions[dataBus.value() & 0xFF];
      AddressingMode mode = W65C02S.addressingModes[dataBus.value() & 0xFF];
      insrBytes[0] = (byte)dataBus.value();
      for(int i = 1; i < mode.length(); i++) {
        addressBus.value(address++);
        clk.value(false);
        clk.value(true);
        insrBytes[i] = (byte)dataBus.value();
      }
      for(int i = 0; i < 3; i++) {
        result.append(i < mode.length() ? String.format(" %02X", insrBytes[i]) : "   ");
      }
      int operand = mode.length() == 3 ? (insrBytes[2] << 8 | (insrBytes[1] & 0xFF)) : insrBytes[1];

      result.append("  ").append(insr).append(" ");
      if(mode.length() == 3) {
        result.append(String.format(mode.format(), (short)((insrBytes[2] << 8) | (insrBytes[1] & 0xFF))));
      }
      else if(mode.length() == 2) {
        result.append(String.format(mode.format(), insrBytes[1]));
      }

      if(count != 1) {
        result.append('\n');
      }
    }
    addressBus.value(addressOld);
    dataBus.value(dataOld);
    rdy.value(rdyOld);
    if(clockWasRunning) {
      clock.start();
    }
    return result.toString();
  }

  /**
   * Read a command line from the terminal, retrying on Ctrl-C and exiting
   * the process cleanly on EOF (Ctrl-D, or real stdin EOF).
   *
   * @return the line read.
   */
  private String readCommandLine() {
    while(true) {
      try {
        return reader.readLine(">>> ");
      }
      catch (UserInterruptException e) {
        // Ctrl-C at the prompt: redisplay prompt
      }
      catch (EndOfFileException e) {
        System.exit(0);
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
    Command command = ResetCommand.instance();
    while(true) {
      try {
        // Run commands one at a time until a Continue command is parsed.
        while(!command.equals(ContinueCommand.instance())) {
          command.execute(this);
          out.format("A: $%02X,  X: $%02X,  Y: $%02X,  S: $%02X,  P: $%02X (%s) cycles: %d%n", cpu.a(), cpu.x(), cpu.y(), cpu.s(), cpu.p(), cpu.status(), cpu.cycleCount());
          out.println(disassemble((short)backplane.address().value(), 1));
          String string = readCommandLine();
          parser = new MonitorParser(new StringReader(string));
          try {
            command = parser.command();
          } catch (ParseException e) {
            out.println(e.getMessage());
            command = NoopCommand.instance();
          }
        }
        command = NoopCommand.instance();

        out.println("(Ctrl-E to pause.)");
        // Terminal.enterRawMode() clears ISIG (like the old raw-mode setup),
        // so Ctrl-C generates no signal here and simply flows to console.write
        // below like any other byte, reaching the simulated UART as input.
        Attributes prevAttributes = terminal.enterRawMode();
        try {
          clock.start();
          while(clock.running()) {
            int c = terminal.reader().read(100L); // blocks up to 100ms; replaces poll+sleep
            if(c == 0x05) { // ^E
              clock.stop();
              while(!sync.value()) {
                clock.cycle();
              }
            }
            else if(c == NonBlockingReader.EOF) {
              break; // stdin closed while a program is running
            }
            else if(c >= 0) {
              console.write(c);
            }
            // c == NonBlockingReader.READ_EXPIRED (-2): plain timeout, loop again
          }
        }
        finally {
          terminal.setAttributes(prevAttributes);
        }

        if(breakpoint != null) {
          out.format("Breakpoint: %s%n", breakpoint);
        }
        else {
          out.println(rdy.value() ? "Paused." : "Stopped.");
        }
      }
      catch (RuntimeException e) {
        throw e;
      }
      catch (Exception e) {
        out.format("ERROR: %s%n", e.getMessage());
      }
    }
  }
}
