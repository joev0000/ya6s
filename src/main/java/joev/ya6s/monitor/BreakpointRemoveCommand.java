package joev.ya6s.monitor;

/**
 * Command to remove a breakpoint from the Monitor.
 */
public class BreakpointRemoveCommand implements Command {
  private final int index;

  /**
   * Create a breakpoint remove command for the given index in the list.
   */
  public BreakpointRemoveCommand(int index) {
    this.index = index;
  }

  /**
   * Remove a breakpoint from the list.
   *
   * @param monitor the Monitor to run the command against.
   * @return null
   */
  @Override
  public Command execute(Monitor monitor) {
    monitor.removeBreakpoint(index);
    return null;
  }
}
