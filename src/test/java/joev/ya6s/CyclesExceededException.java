/* Copyright (C) 2021, 2022 Joseph Vigneau */

package joev.ya6s;

/**
 * An Exception that is thrown if a test exceeds the allowed
 * number of machine cycles.
 */
public class CyclesExceededException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  /**
   * Crete a new CyclesExceededException with the given number
   * of cycles in the exception message.
   *
   * @param cycles the number of cycles that were expected.
   */
  public CyclesExceededException(int cycles) {
    super(String.format("Did not stop in %d cycles.", cycles));
  }
}
