package joev.ya6s.monitor;

import joev.ya6s.Backplane;
import joev.ya6s.W65C02;
import joev.ya6s.signals.Signal;

/**
 * Monitor command to assert the Reset pin on the CPU.
 */
public class ResetCommand implements Command {
  private static final ResetCommand instance = new ResetCommand();

  /**
   * Private constructor- use the instance() method to get an instance.
   */
  private ResetCommand { }

  /**
   * Get an instance of the Reset command.
   *
   * @param an instance of the Reset command.
   */
  public static ResetCommand instance() { return instance; }

  /**
   * Assert the reset signal on the CPU.
   *
   * @param backplane the backplace for synchronization.
   * @param cpu the CPU to assert reset upon.
   * @return a suggeted next Command.  Null.
   */
  @Override
  public Command execute(Backplane backplane, W65C02 cpu) {
    cpu.resb().value(false);
    Signal clock = backplane.clock();
    while(!backplane.sync().value()) {
      clock.value(true);
      clock.value(false);
    }
    cpu.resb().value(true);
    return null;
  }

  /**
   * Return a human-readable representation of this command.
   *
   * @return "reset"
   */
  @Override
  public String toString() {
    return "reset";
  }
}
