/* Copyright (C) 2021, 2022 Joseph Vigneau */

package joev.ya6s.monitor;

/**
 * Monitor command to reset the profile counters.
 */
public class ProfileResetCommand implements Command {
  private static final ProfileResetCommand instance = new ProfileResetCommand();

  /**
   * This class cannot be instantiated.
   */
  private ProfileResetCommand() { }

  /**
   * Get the singleton instance of the ProfileResetCommand.
   *
   * @return the ProfileResetCommand
   */
  public static ProfileResetCommand instance() {
    return instance;
  }

  /**
   * Call the monitor's profileReset method.
   *
   * @return null
   */
  @Override
  public Command execute(Monitor monitor) {
    monitor.profileReset();
    return null;
  }
}
