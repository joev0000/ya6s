package joev.ya6s.monitor;

import java.util.function.Predicate;

import joev.ya6s.W65C02;

/**
 * Command to set a conditional breakpoint.
 */
public class BreakpointWhenCommand implements Command {
  private Predicate<W65C02> predicate;

  /**
   * Create a new breakpoint from the Predicate.
   *
   * @param predicate the breakpoint to test.
   */
  public BreakpointWhenCommand(Predicate<W65C02> predicate) {
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
}
