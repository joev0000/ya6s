package joev.ya6s.expression;

import java.util.function.Function;

import joev.ya6s.W65C02;
import static joev.ya6s.W65C02.CARRY;
import static joev.ya6s.W65C02.ZERO;
import static joev.ya6s.W65C02.NEGATIVE;
import static joev.ya6s.W65C02.OVERFLOW;
import static joev.ya6s.W65C02.DECIMAL;
import static joev.ya6s.W65C02.INTERRUPT_DISABLE;

/**
 * A Function<W65C02, Integer> that returns the value of a register or flag.
 */
public class Register implements Function<W65C02, Integer> {
  private static final Integer zero = Integer.valueOf(0);
  private static final Integer one  = Integer.valueOf(1);

  /** The PC register. */
  public static final Register PC = new Register("PC", cpu -> Integer.valueOf(cpu.pc()));

  /** The A register. */
  public static final Register A  = new Register("A",  cpu -> Integer.valueOf(cpu.a()));

  /** The X register. */
  public static final Register X  = new Register("X",  cpu -> Integer.valueOf(cpu.x()));

  /** The Y register. */
  public static final Register Y  = new Register("Y",  cpu -> Integer.valueOf(cpu.y()));

  /** The stack register. */
  public static final Register S  = new Register("S",  cpu -> Integer.valueOf(cpu.s()));

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
  private final Function<W65C02, Integer> fn;


  /**
   * Create a new Register.  This is a private constructor.
   *
   * @param name the name of the register.
   * @param fn the function that can evaluate the value of the register.
   */
  private Register(String name, Function<W65C02, Integer> fn) {
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
  public Integer apply(W65C02 cpu) {
    return fn.apply(cpu);
  }

  /**
   * Return the name of the register.
   *
   * @return the name of the register.
   */
  @Override
  public String toString() {
   // return register.toString();
   return name;
  }
}
