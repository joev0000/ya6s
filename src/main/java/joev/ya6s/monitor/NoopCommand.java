package joev.ya6s.monitor;

import joev.ya6s.Backplane;
import joev.ya6s.W65C02;

/**
 * A command that does nothing.
 */
public class NoopCommand implements Command {
  public static final NoopCommand instance = new NoopCommand();

  /**
   * Private constructor; get an instance with the instance() method.
   */
  private NoopCommand() { }

  /**
   * Return an instance of the Noop command.
   */
  public static NoopCommand instance() {
    return instance;
  }

  /**
   * Do nothing.
   *
   * @param backplane ignored.
   * @param cpu ignored.
   * @return null
   */
  @Override
  public Command execute(Backplane backplane, W65C02 cpu) {
    return null;
  }

  /**
   * Return a human-readable representation of this command.
   *
   * @return null
   */
  @Override
  public String toString() {
    return "noop";
  }
}
