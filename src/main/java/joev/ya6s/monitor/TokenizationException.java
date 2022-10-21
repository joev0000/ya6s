package joev.ya6s.monitor;

/**
 * An exception thown if an error is encountered while tokenizing the
 * monitor command input.
 */
public class TokenizationException extends Exception {
  public static final long serialVersionUID = 1;

  /**
   * Create a new tokenization exception with the given message.
   *
   * @param message the message to include with the exception
  */
  public TokenizationException(String message) {
    super(message);
  }
}
