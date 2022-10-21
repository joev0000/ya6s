/* Copyright (C) 2021, 2022 Joseph Vigneau */

package joev.ya6s.expression;

import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

import joev.ya6s.W65C02S;

/**
 * A Function<W65C02S, Integer> that returns the value of a register or flag.
 */
public class Register implements Function<W65C02S, Integer> {
  private static final byte NEGATIVE          = (byte)0b10000000;
  private static final byte OVERFLOW          = (byte)0b01000000;
  private static final byte RESERVED          = (byte)0b00100000;
  private static final byte BREAK             = (byte)0b00010000;
  private static final byte DECIMAL           = (byte)0b00001000;
  private static final byte INTERRUPT_DISABLE = (byte)0b00000100;
  private static final byte ZERO              = (byte)0b00000010;
  private static final byte CARRY             = (byte)0b00000001;

  private static final Integer zero = 0;
  private static final Integer one  = 1;

  /** The PC register. */
  public static final Register PC = new Register("PC", cpu -> (cpu.pc() - 1) & 0xFFFF);

  /** The A register. */
  public static final Register A  = new Register("A",  cpu -> cpu.a() & 0xFF);

  /** The X register. */
  public static final Register X  = new Register("X",  cpu -> cpu.x() & 0xFF);

  /** The Y register. */
  public static final Register Y  = new Register("Y",  cpu -> cpu.y() & 0xFF);

  /** The stack register. */
  public static final Register S  = new Register("S",  cpu -> cpu.s() & 0xFF);

  /** The carry flag. */
  public static final Register C  = new Register("C",  cpu -> (cpu.p() & CARRY) == 0 ? zero : one);

  /** The zero flag. */
  public static final Register Z  = new Register("Z",  cpu -> (cpu.p() & ZERO ) == 0 ? zero : one);

  /** The overflow flag. */
  public static final Register V  = new Register("V",  cpu -> (cpu.p() & OVERFLOW) == 0 ? zero : one);

  /** The negative flag. */
  public static final Register N  = new Register("N",  cpu -> (cpu.p() & NEGATIVE) == 0 ? zero : one);

  /** The interrupt disable flag. */
  public static final Register I  = new Register("I",  cpu -> (cpu.p() & INTERRUPT_DISABLE) == 0 ? zero : one);

  /** The decimal mode flag. */
  public static final Register D  = new Register("D",  cpu -> (cpu.p() & DECIMAL) == 0 ? zero : one);

  private final String name;
  private final Function<W65C02S, Integer> fn;

  /**
   * Convenience function to get a Register from its name.
   * 
   * @param s the name of the Register
   * @return An Optional that contains the Register, or an empty if there
   *         is no Register with the requested name.
  */
  public static final Optional<Register> maybeFrom(String s) {
    return switch(s.toUpperCase(Locale.ROOT)) {
      case "PC" -> Optional.of(PC);
      case "A" -> Optional.of(A);
      case "X" -> Optional.of(X);
      case "Y" -> Optional.of(Y);
      case "S" -> Optional.of(S);
      case "C" -> Optional.of(C);
      case "Z" -> Optional.of(Z);
      case "V" -> Optional.of(V);
      case "N" -> Optional.of(N);
      case "I" -> Optional.of(I);
      case "D" -> Optional.of(D);
      default -> Optional.empty();
    };
  }

  /**
   * Create a new Register.  This is a private constructor.
   *
   * @param name the name of the register.
   * @param fn the function that can evaluate the value of the register.
   */
  private Register(String name, Function<W65C02S, Integer> fn) {
    this.name = name;
    this.fn = fn;
  }

  /**
   * Evaluate the register for the given CPU
   *
   * @param cpu the CPU that contains the register to evaluate.
   * @return the value of the register.
   */
  @Override
  public Integer apply(W65C02S cpu) {
    return fn.apply(cpu);
  }

  /**
   * Return the name of the register.
   *
   * @return the name of the register.
   */
  @Override
  public String toString() {
   return name;
  }
}
