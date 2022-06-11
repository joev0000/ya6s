/* Copyright (C) 2021, 2022 Joseph Vigneau */

package joev.ya6s.signals;

/**
 * A group of signals that are typically used together.
 */
public class Bus {
  private final String name;
  private final int width;
  private final String toStringFormat;
  private final int mask;
  private int value;

  /**
   * Create a new Bus with the given name and width (max 32)
   *
   * @param name the name of the Bus.
   * @param width the width of the Bus (clamped between 0 and 32)
   */
  public Bus(String name, int width) {
    this.name = name;
    this.width = Math.min(32, Math.max(0, width));
    this.toStringFormat = String.format("%%s: %%0%dX", width / 4);
    int m = 0;
    for(int i = 0; i < width; i++) {
      m = (m << 1) | 1;
    }
    this.mask = m;
  }

  /**
   * Set the value of the Bus.
   *
   * @param value the value of the Bus.
   */
  public void value(int value) {
    this.value = value & mask;
  }

  /**
   * Get the width of the bus.
   *
   * @return the width of the bus.
   */
  public int width() { return width; }

  /**
   * Get the value of the bus.
   *
   * @return the value of the bus.
   */
  public int value() { return value; }

  /**
   * Get a human-readable representation of this Bus.
   *
   * @return the human-readable representation of this Bus.
   */
  @Override
  public String toString() {
    return String.format(toStringFormat, name, value);
  }
}
