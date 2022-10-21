package joev.ya6s.monitor;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Tokenizer for YA6S monitor commands.
 *
 * Produces a stream of Tokens for the parser.
 */
public class Tokenizer {
  private final PushbackReader input;

  /**
   * Create a new Tokenize that reads from the given Reader.
   *
   * @oaram input the Reader to read token data from.
   * @throws IllegalArgumentException if the input is null.
   */
  public Tokenizer(Reader input) {
    if (input == null) {
      throw new IllegalArgumentException("Tokenizer input cannot be null.");
    }

    this.input = new PushbackReader(input);
  }

  /**
   * Get the next Token, if one is available.
   *
   * @return An Optional that contains the next Token, if there is one.
   * @throws IOException if a problem occurs while reading from the input.
   * @throws TokenizationException if an unexpected chaeacter is encountered.
  */
  public Optional<Token> nextToken() throws IOException, TokenizationException {
    StringBuilder image = new StringBuilder();
    skipWhitespace();
    int c = input.read();
    if(c == -1 || c == 65535) {
      return Optional.empty();
    }

    if(c == '=') {
      return Optional.of(new Token(TokenType.EQUALS, "="));
    }
    if(c == '.') {
      return Optional.of(new Token(TokenType.PERIOD, "."));
    }
    if(c == '!' || c == '<' || c == '>') {
      image.append((char)c);
      return stateRelative(image);
    }
    if(c == '\"') {
      return stateDQString(image);
    }
    if(Character.isDigit(c)) {
      image.append((char)c);
      return stateDecimal(image);
    }
    if((c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')) {
      image.append((char)c);
      return stateHexOrAlphanumeric(image);
    }
    if(Character.isLetter(c)) {
      image.append((char)c);
      return stateAlphanumeric(image);
    }

    throw new TokenizationException("Unexpected character: " + (char)c + " (" + c + ")");
  }

  /**
   * Read a relative expression operator.
   *
   * @param image the token image container.
   * @return a Token of type RELATIVE, if a relative token is found.
   * @throws IOException if an error occurs while reading the input
   * @throws TokenizationException if an unexpected character is encountered.
  */
  private Optional<Token> stateRelative(StringBuilder image) throws IOException, TokenizationException {
    int c = input.read();
    if(c == -1 || Character.isWhitespace(c)) {
      if("!".equals(image.toString())) {
        throw new TokenizationException("Unexpected character: !");
      }
      return Optional.of(new Token(TokenType.RELATIVE, image.toString()));
    }
    if(c == '=') {
      image.append('=');
    }
    else {
      input.unread(c);
    }
    return Optional.of(new Token(TokenType.RELATIVE, image.toString()));
  }

  /**
   * Start reading a decimal number, or look for a hexadecimal value if a-f is
   * encountered.
   *
   * @param image the string builder that captues the image of the token.
   * @return an Optional that contains a Token.
   * @throws IOException if a problem occurs while reading from the input.
   * @throws TokenizationException if an unexpected character is encountered.
   */
  private Optional<Token> stateDecimal(StringBuilder image) throws IOException, TokenizationException {
    while(true) {
      int c = input.read();
      if(c == -1 || Character.isWhitespace(c)) {
        return Optional.of(new Token(TokenType.DECIMAL, image.toString()));
      }

      if(Character.isDigit(c)) {
        image.append((char)c);
        continue;
      }

      if((c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'Z')) {
        image.append((char)c);
        return stateHex(image);
      }

      input.unread(c);
      return Optional.of(new Token(TokenType.DECIMAL, image.toString()));
    }
  }

  /**
   * Read until the next non hexadecimal character, return a hexadecimal
   * token.
   *
   * @param image the string builder that captures the image of the token.
   * @return an Optional that contains a hexadecimal token.
   * @throws IOException if a problem occurs while reading the input.
   * @throws TokenizationException if an unexpected character is encountered.
  */
  private Optional<Token> stateHex(StringBuilder image) throws IOException, TokenizationException {
    while(true) {
      int c = input.read();
      if(c == -1 || Character.isWhitespace(c)) {
        return Optional.of(new Token(TokenType.HEXADECIMAL, image.toString()));
      }
      if((c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F') || Character.isDigit(c)) {
        image.append((char)c);
        continue;
      }

      input.unread(c);
      return Optional.of(new Token(TokenType.HEXADECIMAL, image.toString()));
    }
  }

  /**
   * Read a hexadecimal, alphanumeric, or identifier value.
   *
   * @param image the string builder that captures the image of the token.
   * @return an Optional that contans the token.
   * @throws IOException if a problem occurs while reading the input.
   * @throws TokenizationException if an unecpected character is encountered.
  */
  private Optional<Token> stateHexOrAlphanumeric(StringBuilder image) throws IOException, TokenizationException {
    while(true) {
      int c = input.read();
      if(c == -1 || Character.isWhitespace(c)) {
        return Optional.of(new Token(TokenType.HEXADECIMAL, image.toString()));
      }
      if((c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F') || Character.isDigit(c)) {
        image.append((char)c);
        continue;
      }
      if(Character.isLetter(c)) {
        image.append((char)c);
        return stateAlphanumeric(image);
      }
      if(c == '_' || c == '$') {
        image.append((char)c);
        return stateIdentifier(image);
      }
      input.unread(c);
      return Optional.of(new Token(TokenType.HEXADECIMAL, image.toString()));
    }
  }

  /**
   * Read an alphanumeric or identifier value.
   *
   * @param image the string buffer that captures the image of the token.
   * @return an Optional that contans an alphanumeric or identifier token.
   * @throws IOException if a problem occurs while reading the input.
   * @throws TokenizationException if an unexpected character is encountered.
  */
  private Optional<Token> stateAlphanumeric(StringBuilder image) throws IOException, TokenizationException {
    while(true) {
      int c = input.read();
      if(c == -1 || Character.isWhitespace(c)) {
        return Optional.of(new Token(TokenType.ALPHANUMERIC, image.toString()));
      }
      if(Character.isLetter(c) || Character.isDigit(c)) {
        image.append((char)c);
        continue;
      }
      if(c == '_' || c == '$') {
        image.append((char)c);
        return stateIdentifier(image);
      }
      input.unread(c);
      return Optional.of(new Token(TokenType.ALPHANUMERIC, image.toString()));
    }
  }

  /**
   * Read an identifier token.
   *
   * @param image the string buffer that captures the image of the token.
   * @return an Optional that contains an identifier token.
   * @throws IOException if a problem occurs while reading the input.
   * @throws TokenizationException if an unexpected character is encountered.
  */
  private Optional<Token> stateIdentifier(StringBuilder image) throws IOException, TokenizationException {
    while(true) {
      int c = input.read();
      if(Character.isDigit(c) || Character.isLetter(c) || c == '_' || c == '$') {
        image.append((char)c);
        continue;
      }
      input.unread(c);
      return Optional.of(new Token(TokenType.JAVA_IDENTIFIER, image.toString()));
    }
  }

  /**
   * Read a double-quoted string token.
   *
   * @param image the string buffer that captures the image of the token.
   * @return an Optional that contains an identifier token.
   * @throws IOException if a problem occurs while reading the input.
   * @throws TokenizationException if an unexpected character is encountered.
  */
  private Optional<Token> stateDQString(StringBuilder image) throws IOException, TokenizationException {
    while(true) {
      int c = input.read();
      if(c == -1) {
        throw new TokenizationException("Unterminated string");
      }
      if(c != '"') {
        image.append((char)c);
        continue;
      }
      return Optional.of(new Token(TokenType.STRING, image.toString()));
    }
  }

  /**
   * Skip whitespace in the input.
   *
   * @throws IOException if a problem occurs while reading the input.
  */
  private void skipWhitespace() throws IOException {
    int c;
    for(c = input.read(); Character.isWhitespace(c); c = input.read()) { }
    input.unread(c);
  }

  /**
   * Diagnostic driver program for the Tokenizer.
   *
   * @param args the command line aarguments.  Igored.
   * @throws Exception any uncaught Exception terminates the program.
  */
  public static void main(String[] args) throws Exception {
    Tokenizer tokenizer = new Tokenizer(new java.io.InputStreamReader(System.in, StandardCharsets.UTF_8));
    Optional<Token> next = tokenizer.nextToken();
    while(next.isPresent()) {
      System.out.println(next.get());
      next = tokenizer.nextToken();
    }
  }
}
