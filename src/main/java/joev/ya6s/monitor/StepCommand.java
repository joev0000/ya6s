/* Copyright (C) 2021, 2022 Joseph Vigneau */

package joev.ya6s.monitor;

import joev.ya6s.Backplane;
import joev.ya6s.Clock;
import joev.ya6s.W65C02S;
import joev.ya6s.signals.Signal;

/**
 * Command to run a single step of the processor.
 */
public class StepCommand implements Command {
  private static final StepCommand instance = new StepCommand();

  /**
   * Private constructor- use the instance() method to get an instance.
   */
  private StepCommand() { }

  /**
   * Get an instance of the Step command
   */
  public static StepCommand instance() {
    return instance;
  }

  /**
   * Run a single step of the program.
   *
   * @param monitor the Monitor to run this command against.
   * @return a suggested next Command.
   */
  @Override
  public Command execute(Monitor monitor) {
    Backplane backplane = monitor.backplane();
    W65C02S cpu = monitor.cpu();

    Signal sync = backplane.sync();
    Clock clock = monitor.clock();
    boolean synced = false;
    monitor.updateProfile((short)backplane.address().value());
    while(!cpu.stopped() && !synced) { // or hit a breakpoint
      clock.cycle();
      synced = sync.value();
    }
    if(cpu.stopped()) {
      System.out.println("Stopped.");
    }
    return this;
  }

  /**
   * Return a human-readable representation of this command.
   *
   * @return "step"
   */
  @Override
  public String toString() {
    return "step";
  }
}
