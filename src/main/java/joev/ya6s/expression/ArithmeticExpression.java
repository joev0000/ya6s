package joev.ya6s.expression;

import java.util.function.BiFunction;
import java.util.function.Function;

import joev.ya6s.W65C02S;

/**
 * An ArithmeticExpression evaluates an arithmetic expression in the context
 * of a given CPU.*/
public class ArithmeticExpression implements Function<W65C02S, Integer> {
  /**
   * An enumeration that contains the arithmetic operations.
   */
  public static enum Op {
    ADD  ("+", (a, b) -> Integer.valueOf(a + b)),
    SUB  ("-", (a, b) -> Integer.valueOf(a - b)),
    AND  ("&", (a, b) -> Integer.valueOf(a & b)),
    OR   ("|", (a, b) -> Integer.valueOf(a | b)),
    XOR  ("^", (a, b) -> Integer.valueOf(a ^ b)),
    MUL  ("*", (a, b) -> Integer.valueOf(a * b)),
    DIV  ("/", (a, b) -> Integer.valueOf(a / b)),
    MOD  ("%", (a, b) -> Integer.valueOf(a % b));

    private final String symbol;
    private final BiFunction<Integer, Integer, Integer> fn;
    Op(String symbol, BiFunction<Integer, Integer, Integer> fn) {
      this.symbol = symbol;
      this.fn = fn;
    }

    /**
     * Return the symbol of the operation
     *
     * @return the symbol of the operation.
     */
    @Override
    public String toString() {
      return symbol;
    }

    /**
     * Apply the operator to the input values.
     *
     * @param a the first input value.
     * @param b the second input value.
     * @return the result of the operation.
     */
    public Integer apply(Integer a, Integer b) {
      return fn.apply(a, b);
    }
  }

  private final Function<W65C02S, Integer> a;
  private final Function<W65C02S, Integer> b;
  private final Op op;

  /**
   * Create an arithmetic expression based on an operation and two inputs.
   *
   * @param op the operation for this expression.
   * @param a the first input value.
   * @param b the second input value.
   */
  public ArithmeticExpression(Op op, Function<W65C02S, Integer> a, Function<W65C02S, Integer> b) {
    this.op = op;
    this.a = a;
    this.b = b;
  }

  /**
   * Apply the operation to the inputs in the context of the CPU.
   *
   * @param cpu the context of the expression.
   * @return the result of the operation.
   */
  @Override
  public Integer apply(W65C02S cpu) {
    return op.apply(a.apply(cpu), b.apply(cpu));
  }

  /**
   * A String of the form "a op b".
   *
   * @return a human readable representation of the expression.
   */
  @Override
  public String toString() {
    return String.format("%s %s %s", a, op, b);
  }
}
