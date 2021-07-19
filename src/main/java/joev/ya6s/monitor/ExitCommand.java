package joev.ya6s.monitor;

import joev.ya6s.Backplane;
import joev.ya6s.W65C02;

/**
 * Command used to exit the monitor.
 */
public class ExitCommand implements Command {
  private static final ExitCommand instance = new ExitCommand();

  /**
   * Private constructor; use the instance() method to get an instance.
   */
  private ExitCommand() { }

  /**
   * Get an instance of an Exit command.
   *
   * @return an Exit command.
   */
  public static ExitCommand instance() {
    return instance;
  }

  /**
   * Exits from the monitor.
   *
   * @param monitor ignored
   * @return nothing, since this uses System.exit(int)
   */
  @Override
  public Command execute(Monitor monitor) {
    System.exit(0);
    return null;
  }

  /**
   * Return an human-friendly representation of this command.
   *
   * @return "exit"
   */
  @Override
  public String toString() {
    return "exit";
  }
}
