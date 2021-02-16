package joev.ya6s.monitor;

import joev.ya6s.Backplane;
import joev.ya6s.W65C02;
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
   * @param backplane the Backplane of the system.
   * @param cpu the CPU of the system.
   * @return a suggested next Command.
   */
  @Override
  public Command execute(Backplane backplane, W65C02 cpu) {
    Signal sync = backplane.sync();
    Signal clock = backplane.clock();
    boolean synced = false;
    while(!cpu.stopped() && !synced) { // or hit a breakpoint
      clock.value(true);
      clock.value(false);
      synced = sync.value();
    }
    if(cpu.stopped()) {
      System.out.println("Stopped.");
    }
    System.out.format("PC: $%04X  A: $%02X  X: $%02X  Y: $%02X  S: $%02X  P: $%02X (%s)%n", cpu.pc(), cpu.a(), cpu.x(), cpu.y(), cpu.s(), cpu.p(), cpu.status());
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