/* Copyright (C) 2021, 2022 Joseph Vigneau */

package joev.ya6s.monitor;

public class ProfileOnCommand implements Command {
  public static final ProfileOnCommand instance = new ProfileOnCommand();

  /**
   * This class cannot be instantiated.
   */
  private ProfileOnCommand() { }

  /**
   * Get the singleton instance of the ProfileOnCommand.
   *
   * @return the ProfileOnCommand
   */
  public static ProfileOnCommand instance() {
    return instance;
  }

  /**
   * Set the monitor's profiling flag.
   *
   * @return null
   */
  @Override
  public Command execute(Monitor monitor) {
    monitor.profiling(true);
    return null;
  }
}
