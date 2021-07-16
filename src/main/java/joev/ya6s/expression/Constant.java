package joev.ya6s.expression;

import java.util.function.Function;

import joev.ya6s.W65C02;

/**
 * A Function<W65C02, Integer> that returns a constant value regardless
 * of the state of the CPU.
 */
public class Constant implements Function<W65C02, Integer> {
  private final Integer value;

  /**
   * Create a new contant that always returns the provided value.
   *
   * @param value the value of the constant.
   */
  public Constant(Integer value) {
    this.value = value;
  }

  /**
   * Return the contant value.
   *
   * @param cpu the CPU to evaluate this constant against.
   * @return the constant value.
   */
  @Override
  public Integer apply(W65C02 cpu) {
    return value;
  }

  /**
   * Return a hex representation of the contant.
   *
   * @return the hex value of the constant.
   */
  @Override
  public String toString() {
    return Integer.toHexString(value);
  }
}

