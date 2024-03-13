/* Copyright (C) 2021, 2022 Joseph Vigneau */

package joev.ya6s.monitor;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Optional;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import joev.ya6s.W65C02S;
import joev.ya6s.expression.Constant;
import joev.ya6s.expression.Register;
import joev.ya6s.expression.RelationalExpression;


/**
 * Parser for Monitor commands.
 */
public final class MonitorParser {
  /**
   * Utility method to create a Supplier that creates a ParseException
   * with the given message.
   *
   * @param message the message to include in the exception
   * @return a ParseException with the given message
  */
  private static Supplier<ParseException> parseException(String message) {
    return () -> new ParseException(message);
  }

  /**
   * Utility method to parse a String as a decimal number.
   *
   * @param s the String to parse.
   * @return the parsed int.
  */
  private static int parseDec(String s) {
    return Integer.parseInt(s, 10);
  }

  /**
   * Utility method to parse a String as a hexadecimal integer.
   *
   * @param s the String to parse
   * @return the parsed int.
  */
  private static int parseHex(String s) {
    return Integer.parseInt(s, 16);
  }

  /** The set of token types that are valid for a command.  */
  private static final Set<TokenType> commandTypes = EnumSet.of(
      TokenType.ALPHABETICAL,
      TokenType.ALPHANUMERIC,
      TokenType.HEXADECIMAL,
      TokenType.JAVA_IDENTIFIER
  );

  /** The set of token types that are valid for a Java type name  */
  private static final Set<TokenType> typeNameTypes = EnumSet.of(
      TokenType.ALPHABETICAL,
      TokenType.ALPHANUMERIC,
      TokenType.HEXADECIMAL,
      TokenType.JAVA_IDENTIFIER
  );
  /** The set of token types that are valid for hexadecimal numbers. */
  private static final Set<TokenType> numberTypes = EnumSet.of(
      TokenType.DECIMAL,
      TokenType.HEXADECIMAL
  );
  /** The set of token types that are valid for relative symbols. */
  private static final Set<TokenType> relativeTypes = EnumSet.of(
      TokenType.RELATIVE,
      TokenType.EQUALS
  );

  /** The period token type. */
  private static final Set<TokenType> periodType = EnumSet.of(TokenType.PERIOD);

  /** The equals token type. */
  private static final Set<TokenType> equalsType = EnumSet.of(TokenType.EQUALS);

  /** The decimal token type. */
  private static final Set<TokenType> decimalType = EnumSet.of(TokenType.DECIMAL);

  /** The alphanumeric token type. */
  private static final Set<TokenType> alphanumericType = EnumSet.of(TokenType.ALPHANUMERIC);

  /** All token types. */
  private static final Set<TokenType> allTypes = EnumSet.allOf(TokenType.class);

  // End of static section.
  
  /** The Tokeizer that breaks input into tokens.  */
  private final Tokenizer tokenizer;

  /**
   * A holding area for tokens that have already been read from the
   * tokeizer, but have not yet been accepted.
   */
  private Optional<Token> pushedToken;

  /**
   * Create a new monitor command parser that reads input from the provided
   * Reader.
   *
   * @param input the source of monitor command characters.
  */
  public MonitorParser(Reader input) {
    if(input == null) {
      throw new IllegalArgumentException("MonitorReader input cannot be null.");
    }
    this.tokenizer = new Tokenizer(input);
    this.pushedToken = Optional.empty();
  }

  /**
   * Get the next token from the input, if one is available.
   *
   * @throws TokenizationException if the tokenizer encounters an error.
   * @throws IOException if the underlying input Reader encounters an error.
   */
  private Optional<Token> nextToken() throws TokenizationException, IOException {
    Optional<Token> opt;
    if(pushedToken.isPresent()) {
      opt = pushedToken;
      pushedToken = Optional.empty();
    }
    else {
      opt = tokenizer.nextToken();
    }
    return opt;
  }

  /**
   * Push a token back into the stream, so it will be re-read next.
   *
   * @param t the token to push back on to the stream.
  */
  private void pushToken(Optional<Token> t) {
    pushedToken = t;
  }

  /**
   * Parse a monitor command from the input.
   *
   * @return a monitor command.
   * @throws ParseException if there was an unexpected token while parsing.
   */
  public Command command() throws ParseException {
    Token token = one(commandTypes);
    String image = token.image().toLowerCase(Locale.ROOT);
    if("exit".equals(image)) {
      return ExitCommand.instance();
    }
    if("cont".equals(image) || "c".equals(image)) {
      return ContinueCommand.instance();
    }
    if("reset".equals(image)) {
      return ResetCommand.instance();
    }
    if("step".equals(image) || "s".equals(image)) {
      return StepCommand.instance();
    }
    if("load".equals(image)) {
      return load();
    }
    if("attach".equals(image)) {
      return attach();
    }
    if("write".equals(image) || "w".equals(image)) {
      return write();
    }
    if("read".equals(image) || "r".equals(image)) {
      return read();
    }
    if("disassemble".equals(image) || "d".equals(image)) {
      return disassemble();
    }
    if("profile".equals(image)) {
      return profile();
    }
    if("breakpoint".equals(image) || "break".equals(image) || "b".equals(image)) {
      return breakpoint();
    }
    throw new ParseException("Unknown command " + token.image());
  }

  /**
   * Return the next token if it is one of the given types.
   *
   * @param types the set of TokenTypes that are expected.
   * @return The next token, if its type is one of the provided types.
   * @throws ParseException if there is an error during tokenization or
   *   if the next token is not one of the given types.
  */
  private Token one(Set<TokenType> types) throws ParseException {
    try {
      Optional<Token> opt = nextToken();
      Token t = opt.orElseThrow(parseException("Expected a token, but found none."));
      if(!types.contains(t.type())) {
        throw new ParseException("Unexpected token " + t.image() + " (" + t.type() + ")");
      }
      return t;
    }
    catch (IOException ioe) {
      throw new ParseException("IO error while parsing.", ioe);
    }
    catch (TokenizationException te) {
      throw new ParseException("Tokenization error while parsing.", te);
    }
  }

  /**
   * Optionally return the next token if it is one of the given types.
   *
   * Pushes the token back on to the stream if it is not one of the given
   * types.
   *
   * @param types the set of TokenTypes that are expected.
   * @return an Optional containing the next token if its type is one of
   *   the provided types, an empty Optional otherwise.
   * @throws ParseException if there is an error during tokenization.
  */
  private Optional<Token> maybe(Set<TokenType> types) throws ParseException {
    try {
      return nextToken().map(t -> {
        if(types.contains(t.type())) {
          return t;
        }
        pushToken(Optional.of(t));
        return null;
      });
    }
    catch (IOException ioe) {
      throw new ParseException("IO error while parsing.", ioe);
    }
    catch (TokenizationException te) {
      throw new ParseException("Tokenization error while parsing.", te);
    }
  }

  /**
   * Return a series of consecutive tokens whose types are in the set of
   * given types. Note, the tokens do not need to be all of the same
   * individual type, rather each individual token needs to be of one
   * of the types of the given set of types.
   *
   * @param types the set of types of tokens that will appear in the
   *   retuned List.
   * @return a List of consecutibe tokens whose types are provided in the
   *   types parameter.
   * @throws ParseException if there is a tokenization error.
  */
  private List<Token> zeroOrMore(Set<TokenType> types) throws ParseException {
    try {
      List<Token> tokens = new ArrayList<>();

      while(true) {
        Optional<Token> opt = nextToken();
        if(opt.isEmpty()) { // TODO: Can this loop use Stream.generate(nextToken)?
          break;
        }
        Token t = opt.get();
        if(!types.contains(t.type())) {
          pushToken(opt);
          break;
        }
        tokens.add(t);
      }
      return Collections.unmodifiableList(tokens);
    }
    catch (IOException ioe) {
      throw new ParseException("IO error while parsing.", ioe);
    }
    catch (TokenizationException te) {
      throw new ParseException("Tokenization error while parsing.", te);
    }
  }

  /**
   * Parse the arguments of a read command.
   *
   * @return a ReadCommand with the proper argument values.
   * @throws ParseException if there is an underlying tokenization error.
  */
  private ReadCommand read() throws ParseException {
    // read start [end]
    short start = (short)parseHex(one(numberTypes).image());
    Optional<Token> end = maybe(numberTypes);
    return end.map(t -> new ReadCommand(start, (short)parseHex(t.image())))
      .orElse(new ReadCommand(start));
  }

  /**
   * Parse the arguments of a write command.
   *
   * @return a WriteCommand with the proper argument values.
   * @throws ParseException if there is an underlying tokenization error.
  */
  private WriteCommand write() throws ParseException {
    // write addr [byte]*
    short address = (short)parseHex(one(numberTypes).image());
    List<Byte> bytesList = new ArrayList<>();
    for(Token t: zeroOrMore(numberTypes)) {
      bytesList.add((byte)parseHex(t.image()));
    }
    byte[] bytes = new byte[bytesList.size()];
    int i = 0;
    for(byte b: bytesList) {
      bytes[i] = b;
      i++;
    }
    return new WriteCommand(address, bytes);
  }

  /**
   * Parse a Java class name, including the package seperators.
   *
   * @return a String that may be a Java class name.
   * @throws ParseException if there is an underlying tokenization error.
  */
  private String className() throws ParseException {
    StringBuilder name = new StringBuilder();
    name.append(one(typeNameTypes).image());
    while(maybe(periodType).isPresent()) {
      name.append(".").append(one(typeNameTypes).image());
    }
    return name.toString();
  }

  /**
   * Parse a series of name=value pairs into a Map.
   *
   * @return a Map containing name/value pairs
   * @throws ParseException if there is an underlying tokeization error.
   * */
  private Map<String, String> nameValuePairs() throws ParseException {
    Map<String, String> pairs = new HashMap<>();
    Optional<Token> opt = maybe(typeNameTypes);
    while(opt.isPresent()) {
      String name = opt.get().image();
      one(equalsType);
      String value = one(allTypes).image();
      pairs.put(name, value);
      opt = maybe(typeNameTypes);
    }
    return Collections.unmodifiableMap(pairs);
  }

  /**
   * Parse the arguments of an attach command.
   *
   * @return an AttachCommand with the provided arguments
   * @throws ParseException if there is an underlying tokenization error.
  */
  private AttachCommand attach() throws ParseException {
    String className = className();
    Map<String, String> options = nameValuePairs();

    return new AttachCommand(className, options);
  }

  /**
   * Parse the arguments of a load command.
   *
   * @return a LoadCommand with the provided arguments.
   * @throws ParseException if there is an underlying tokenization error.
   */
  private LoadCommand load() throws ParseException {
    short start = (short)parseHex(one(numberTypes).image());
    String path = one(allTypes).image();
    return new LoadCommand(start, path);
  }

  /**
   * Parse the arguments of a disassemble command.
   *
   * @return a DisassembleCommand with the provided arguments.
   * @throws ParseException if there is an underlying tokenization error.
  */
  private DisassembleCommand disassemble() throws ParseException {
    short address = (short)parseHex(one(numberTypes).image());
    int count = maybe(numberTypes).map(t -> parseDec(t.image())).orElse(10);
    return new DisassembleCommand(address, count);
  }

  /**
   * Parse the arguments of a profile command.
   *
   * @return a profile subcommand object with the provided arguments.
   * @throws ParseException if there is an underlying tokenization error.
  */
  private Command profile() throws ParseException {
    String subcommand = one(alphanumericType).image().toLowerCase(Locale.ROOT);
    if("on".equals(subcommand)) {
      return ProfileOnCommand.instance();
    }
    if("off".equals(subcommand)) {
      return ProfileOffCommand.instance();
    }
    if("reset".equals(subcommand)) {
      return ProfileResetCommand.instance();
    }
    if("show".equals(subcommand)) {
      int maxLines = maybe(decimalType).map(t -> parseDec(t.image())).orElse(Integer.MAX_VALUE);
      return new ProfileShowCommand(maxLines);
    }
    throw new ParseException("Unknown subcommand " + subcommand);
  }

  /**
   * Parse a breakpoint contant or register expression.
   *
   * @return the Function that represents the constant or register expression.
   * @throws ParseException if there is an underlying tokenization error.
  */
  private Function<W65C02S, Integer> constOrReg() throws ParseException {
    Optional<Function<W65C02S, Integer>> f =
      maybe(numberTypes)
        .map(t -> parseHex(t.image()))
        .map(Constant::new);

    if(f.isEmpty()) {
      f = maybe(alphanumericType).map(Token::image).flatMap(Register::maybeFrom);
    }

    return f.orElseThrow(parseException("Expected number or register."));
  }

  /**
   * Parse a relational expression.
   *
   * constant|register op constant|register
   *
   * @return a Predicate that can evaluate the expression.
   * @throws ParseException if there is an underlying tokenization error.
   */
  private Predicate<W65C02S> relationalExpression() throws ParseException {
    Function<W65C02S,Integer> lhs = constOrReg();
    RelationalExpression.Op op =
      RelationalExpression.Op.maybeFrom(one(relativeTypes).image())
        .orElseThrow(parseException("Expected relational operator."));
    Function<W65C02S,Integer> rhs = constOrReg();

    return new RelationalExpression(op, lhs, rhs);
  }

  /**
   * Parse a breakpoint command.
   *
   * @return a breakpoint subcommand with the provided arguments.
   * @throws ParseException if there is an underlying tokenization error.
  */
  private Command breakpoint() throws ParseException {
    String subcommand = one(alphanumericType).image().toLowerCase(Locale.ROOT);
    if("list".equals(subcommand)) {
      return BreakpointListCommand.instance();
    }
    if("remove".equals(subcommand)) {
      return new BreakpointRemoveCommand(parseDec(one(decimalType).image()));
    }
    if("at".equals(subcommand)) {
      return new BreakpointWhenCommand(
          new RelationalExpression(
            RelationalExpression.Op.EQUALS,
            Register.PC,
            new Constant(parseHex(one(numberTypes).image()))));
    }
    if("when".equals(subcommand)) {
      return new BreakpointWhenCommand(relationalExpression());
    }
    throw new ParseException("Unknown subcommand " + subcommand);
  }
}
