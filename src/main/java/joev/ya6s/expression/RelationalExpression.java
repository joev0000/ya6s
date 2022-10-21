/* Copyright (C) 2021, 2022 Joseph Vigneau */

package joev.ya6s.expression;

import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import joev.ya6s.W65C02S;

/**
 * A RelationalExpression compares the values of two arithmetic expressions
 * in the context of a given CPU.
 */
public class RelationalExpression implements Predicate<W65C02S> {
  /**
   * An enumeration that contains the relational operations.
   */
  public enum Op {
    EQUALS                 ("=",  (a, b) -> a.equals(b)),
    NOT_EQUALS             ("!=", (a, b) -> !a.equals(b)),
    GREATER_THAN           (">",  (a, b) -> a > b),
    LESS_THAN              ("<",  (a, b) -> a < b),
    GREATER_THAN_OR_EQUALS (">=", (a, b) -> a >= b),
    LESS_THAN_OR_EQUALS    ("<=", (a, b) -> a <= b);

    private final String symbol;
    private final BiPredicate<Integer, Integer> predicate;
    Op(String symbol, BiPredicate<Integer, Integer> predicate) {
      this.symbol = symbol;
      this.predicate = predicate;
    }

    /**
     * Evaluate the expression.
     *
     * @param a the left-hand side.
     * @param b the right-hand side.
     * @return true if the relationship holds.
     */
    public boolean test(Integer a, Integer b) {
      return predicate.test(a, b);
    }

    /**
     * Return the symbol of the operation.
     *
     * @return the symbol of the operation.
     */
    @Override
    public String toString() {
      return symbol;
    }

    /**
     * Attempt to return an Op from a String.
     *
     * @param s the String that matches the name of the Op.
     * @return an Optional containing the Op if the String matches.
    */
    public static Optional<Op> maybeFrom(String s) {
      return switch(s) {
        case "=" -> Optional.of(EQUALS);
        case "!=" -> Optional.of(NOT_EQUALS);
        case ">" -> Optional.of(GREATER_THAN);
        case "<" -> Optional.of(LESS_THAN);
        case ">=" -> Optional.of(GREATER_THAN_OR_EQUALS);
        case "<=" -> Optional.of(LESS_THAN_OR_EQUALS);
        default -> Optional.empty();
      };
    }
  }

  private final Function<W65C02S, Integer> a;
  private final Function<W65C02S, Integer> b;
  private final Op op;

  /**
   * Create a new relational expression by comparing the value of arithmetic
   * expressions.
   *
   * @param op the relational operation
   * @param a the left-hand side of the expression.
   * @param b the right-hand side of the expression.
   */
  public RelationalExpression(Op op, Function<W65C02S, Integer> a, Function<W65C02S, Integer> b) {
    this.a = a;
    this.b = b;
    this.op = op;
  }

  /**
   * Evaluate the relational expression in the context of a CPU.
   *
   * @param cpu the CPU used as the context of the evaluation.
   * @return true if the relationship holds.
   */
  @Override
  public boolean test(W65C02S cpu) {
    return op.test(a.apply(cpu), b.apply(cpu));
  }

  /**
   * A String of the form "a op b"
   *
   * @return a human-readable representation of the expression.
   */
  @Override
  public String toString() {
    return String.format("%s %s %s", a, op, b);
  }
}
