package joev.ya6s.monitor;

public class ProfileOffCommand implements Command {
  public static final ProfileOffCommand instance = new ProfileOffCommand();

  /**
   * This class cannot be instantiated.
   */
  private ProfileOffCommand() { }

  /**
   * Get the singleton instance of the ProfileOffCommand.
   *
   * @return the ProfileOffCommand
   */
  public static ProfileOffCommand instance() {
    return instance;
  }

  /**
   * Clear the monitor's profiling flag.
   *
   * @return null
   */
  @Override
  public Command execute(Monitor monitor) {
    monitor.profiling(false);
    return null;
  }
}
