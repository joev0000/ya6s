/* Copyright (C) 2021, 2022 Joseph Vigneau */

package joev.ya6s.monitor;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.IntStream;

/**
 * Display the profile metrics
 */
public class ProfileShowCommand implements Command {
  private int maxLines;

  /**
   * Create a new Profile Show command to display up to
   * the given number of lines of metrics.
   *
   * @param maxLines the maximum nubmer of lines to show.
   */
  public ProfileShowCommand(int maxLines) {
    this.maxLines = maxLines;
  }

  /**
   * Show the profile data, sorted by the number of cycles
   * spent executing each instruction.
   *
   * @param monitor the Monitor to execute this command against.
   */
  @Override
  public Command execute(Monitor monitor) {

    // An internal record type to facilitate streaming.
    record Count(int address, long counter) { }

    long[] profile = monitor.profile();
    IntStream
      .range(0, profile.length)
      .filter(i -> profile[i] != 0)
      .mapToObj(i -> new Count(i, profile[i]))
      .sorted(Comparator.comparingLong(c -> ((Count)c).counter).reversed())
      .limit(maxLines)
      .forEach(c -> System.out.format("$%04X: %16d%n", c.address, c.counter));

    return null;
  }

  /**
   * Compare this ProfileShowCommand with another Object.
   *
   * @param other the other Object to compare
   * @return true if the other Object is a ProfileShowCommand with the same value.
   */
  @Override
  public boolean equals(Object other) {
    if(other instanceof ProfileShowCommand o) {
      return this.maxLines == o.maxLines;
    }
    return false;
  }

  /**
   * Return the hash code of this ProfileShowCommand.
   *
   * @return the hash code of this ProfileShowCommand.
   */
  @Override
  public int hashCode() {
    return maxLines;
  }
}
