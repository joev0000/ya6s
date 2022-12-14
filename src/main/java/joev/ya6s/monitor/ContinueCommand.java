/* Copyright (C) 2021, 2022 Joseph Vigneau */

package joev.ya6s.monitor;

import joev.ya6s.Clock;
import joev.ya6s.W65C02S;

/**
 * The Continue command.
 */
public class ContinueCommand implements Command {
  private static final ContinueCommand instance = new ContinueCommand();

  /**
   * Private constructor so this cannot be instantiated.
   */
  private ContinueCommand() { }

  /**
   * Return the static instance of this command.
   *
   * @return the continue Command.
   */
  public static ContinueCommand instance() {
    return instance;
  }

  /**
   * Execute the command; run until the CPU is stopped.
   *
   * @param monitor the Monitor which will execute this command.
   * @return the next suggested Command, or null.
   */
  @Override
  public Command execute(Monitor monitor) {
    Clock clock = monitor.clock();
    W65C02S cpu = monitor.cpu();

    while(!cpu.stopped()) { // or hit a breakpoint
      clock.cycle();
    }
    if(cpu.stopped()) {
      System.out.println("\nStopped.");
    }
    System.out.format("PC: $%04X  A: $%02X  X: $%02X  Y: $%02X  S: $%02X  P: $%02X (%s) cycles: %d%n", cpu.pc(), cpu.a(), cpu.x(), cpu.y(), cpu.s(), cpu.p(), cpu.status(), cpu.cycleCount());
    return null;
  }

  /**
   * Get a human-readable representation of this command.
   *
   * @return "cont"
   */
  @Override
  public String toString() {
    return "cont";
  }
}
