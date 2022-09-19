/* Copyright (C) 2021, 2022 Joseph Vigneau */

package joev.ya6s;

import java.util.function.Consumer;
/**
 * Record class used to parameterize tests.
 *
 * TODO: When JDK 16 is released, change this to
 *
 * public record Parameters(String name, String program,
 *   int cycles, Consumer<W65C02S>... asserts) { }
 */
public class Parameters {
  private final String name;
  private final String program;
  private final int cycles;
  private Consumer<W65C02S>[] asserts;
  @SuppressWarnings("varargs")
  @SafeVarargs
  public Parameters(String name, String program, int cycles, Consumer<W65C02S>... asserts) {
    this.name = name;
    this.program = program;
    this.cycles = cycles;
    this.asserts = asserts;
  }
  public String name() { return name; }
  public String program() { return program; }
  public int cycles() { return cycles; }
  public Consumer<W65C02S>[] asserts() { return asserts.clone(); }
  public String toString() { return name; }

  @SafeVarargs
  public static Parameters params(String name, String program, int cycles, Consumer<W65C02S>... asserts) {
    return new Parameters(name, program, cycles, asserts);
  }
}
