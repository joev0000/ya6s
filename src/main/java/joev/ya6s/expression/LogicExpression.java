package joev.ya6s.expression;

import java.util.function.BiPredicate;
import java.util.function.Predicate;
import joev.ya6s.W65C02;

/**
 * A LogicExpression evaluates the truth of two sub-expressions.
 */
public class LogicExpression implements Predicate<W65C02> {
  /**
   * An enumeration that contains the logic operations.
   */
  enum Op {
    AND ("AND", (a, b) -> a && b),
    OR  ("OR",  (a, b) -> a || b),
    XOR ("XOR", (a, b) -> a != b);

    private final String symbol;
    private final BiPredicate<Boolean, Boolean> predicate;
    Op(String symbol, BiPredicate<Boolean, Boolean> predicate) {
      this.symbol = symbol;
      this.predicate = predicate;
    }

    /**
     * Evaluate the expression.
     *
     * @param a the first expression
     * @param b the second expression
     * @return the result of evaluating the logic operation.
     */
    public boolean test(Boolean a, Boolean b) {
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
  }

  private final Op op;
  private final Predicate<W65C02> a;
  private final Predicate<W65C02> b;

  /**
   * Create a new logic expression using the given operator and sub-expressions.
   */
  public LogicExpression(LogicExpression.Op op, Predicate<W65C02> a, Predicate<W65C02> b) {
    this.a = a;
    this.b = b;
    this.op = op;
  }

  /**
   * Evaluate the expression in the context of a CPU.
   *
   * @param cpu the CPU used as the context of the evaluation.
   * @return the result of the logic operation.
   */
  @Override
  public boolean test(W65C02 cpu) {
    return op.test(a.test(cpu), b.test(cpu));
  }

  /**
   * A String of the form "a op b"
   *
   * @return a human readable representation of the expression.
   */
  @Override
  public String toString() {
    return String.format("%s %s %s", a, op, b);
  }
}

