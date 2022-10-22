/* Copyright (C) 2021, 2022 Joseph Vigneau */

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

  /**
   * Compare this BreakpointRemoveCommand with another Object.
   *
   * @param other the other Object to compare
   * @return true if the other Object is a BreakpointRemoveCommand with the same value.
   */
  @Override
  public boolean equals(Object other) {
    if(other instanceof BreakpointRemoveCommand o) {
      return this.index == o.index;
    }
    return false;
  }

  /**
   * Return the hash code of this BreakpointRemoveCommand.
   *
   * @return the hash code of this BreakpointRemoveCommand.
   */
  @Override
  public int hashCode() {
    return index;
  }
}
