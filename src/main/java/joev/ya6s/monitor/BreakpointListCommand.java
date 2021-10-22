package joev.ya6s.monitor;

import joev.ya6s.W65C02S;
import java.util.function.Predicate;

/**
 * Command to list the breakpoints.
 */
public class BreakpointListCommand implements Command {
  /** The singleton instance. */
  private static final BreakpointListCommand instance = new BreakpointListCommand();

  /**
   * Private constructor.
   */
  private BreakpointListCommand() {
  }

  /**
   * Get the instance of this command.
   */
  public static BreakpointListCommand instance() {
    return instance;
  }

  /**
   * Execute the command- list the breakpoints.
   *
   * @param monitor the Monitor to run the command againt.
   * @return null
   */
  @Override
  public Command execute(Monitor monitor) {
    int i = 0;
    for(Predicate<W65C02S> p: monitor.listBreakpoints()) {
      System.out.format("%d: %s%n", i++, p);
    }
    return null;
  }
}


