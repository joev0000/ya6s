package joev.ya6s.monitor;

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
   * @param monitor ignored.
   * @return null
   */
  @Override
  public Command execute(Monitor monitor) {
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
