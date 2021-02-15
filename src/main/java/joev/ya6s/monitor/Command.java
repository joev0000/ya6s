package joev.ya6s.monitor;

import joev.ya6s.Backplane;
import joev.ya6s.W65C02;

/**
 * A Monitor command that can be executed.
 */
public interface Command {
  /**
   * Execute the command with the given Backplane and CPU.
   *
   * @return a suggested next Command, or null if there is no suggestion.
   */
  public Command execute(Backplane backplane, W65C02 cpu);
}
