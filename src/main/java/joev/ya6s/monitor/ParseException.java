/* Copyright (C) 2021, 2022 Joseph Vigneau */

package joev.ya6s.monitor;

/**
 * Exception class used for any errors during parsing.
 */
public class ParseException extends Exception {
  private final static long serialVersionUID = 1L;
 
  /**
   * Create a new ParseException with the given message.
   *
   * @param message the message to include in the Exception.
  */
  public ParseException(String message) {
    super(message);
  }

  /**
   * Create a new ParseException with the given messaage and root cause.
   *
   * @param message the message to include in the exception.
   * @param throwable the root cause of the exception.
  */
  public ParseException(String message, Throwable throwable) {
    super(message, throwable);
  }
}
