/* Copyright (C) 2021, 2022 Joseph Vigneau */

package joev.ya6s.expression;

import java.util.function.Function;

import joev.ya6s.W65C02S;

/**
 * A Function<W65C02S, Integer> that returns a constant value regardless
 * of the state of the CPU.
 */
public class Constant implements Function<W65C02S, Integer> {
  private final int value;

  /**
   * Create a new constant that always returns the provided value.
   *
   * @param value the value of the constant.
   */
  public Constant(int value) {
    this.value = value;
  }

  /**
   * Return the constant value.
   *
   * @param cpu the CPU to evaluate this constant against.
   * @return the constant value.
   */
  @Override
  public Integer apply(W65C02S cpu) {
    return Integer.valueOf(value);
  }

  /**
   * Return a hex representation of the constant.
   *
   * @return the hex value of the constant.
   */
  @Override
  public String toString() {
    return Integer.toHexString(value);
  }

  /**
   * Compare this Constant with another Object.
   *
   * @param other the other Object to compare
   * @return true if the other Object is a Constant with the same value.
   */
  @Override
  public boolean equals(Object other) {
    if(other instanceof Constant o) {
      return this.value == o.value;
    }
    return false;
  }

  /**
   * Return the hash code of this Constant.
   *
   * @return the hash code of this Constant.
   */
  @Override
  public int hashCode() {
    return this.value;
  }
}

