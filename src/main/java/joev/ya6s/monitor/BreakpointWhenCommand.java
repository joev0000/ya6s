/* Copyright (C) 2021, 2022 Joseph Vigneau */

package joev.ya6s.monitor;

import java.util.function.Predicate;

import joev.ya6s.W65C02S;

/**
 * Command to set a conditional breakpoint.
 */
public class BreakpointWhenCommand implements Command {
  private final Predicate<W65C02S> predicate;

  /**
   * Create a new breakpoint from the Predicate.
   *
   * @param predicate the breakpoint to test.
   */
  public BreakpointWhenCommand(Predicate<W65C02S> predicate) {
    this.predicate = predicate;
  }

  /**
   * Add the breakpoint to the monitor.
   *
   * @param monitor the Monitor to run the command against.
   * @return null
   */
  @Override
  public Command execute(Monitor monitor) {
    monitor.addBreakpoint(predicate);
    return null;
  }

  @Override
  public boolean equals(Object other) {
    if(other instanceof BreakpointWhenCommand o) {
      return this.predicate.equals(o.predicate);
    }
    return false;
  }

  /**
   * Compare this BreakpointWhenCommand with another Object.
   *
   * @param other the other Object to compare
   * @return true if the other Object is a BreakpointWhenCommand with the same value.
   */
  @Override
  public int hashCode() {
    return predicate.hashCode();
  }

  /**
   * Return the hash code of this BreakpointWhenCommand.
   *
   * @return the hash code of this BreakpointWhenCommand.
   */
  @Override
  public String toString() {
    return predicate.toString();
  }
}
