package joev.ya6s;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.Consumer;

/**
 * Useful assertions for CPU unit tests.
 */
public class Assertions {

  /**
   * Assert that the Negative flag is set.
   *
   * @param cpu the CPU
   */
  public static void assertNegative(W65C02 cpu) {
    if((cpu.p() & W65C02.NEGATIVE) == 0) {
      throw new AssertionError("Negative flag is not set.");
    }
  }

  /**
   * Assert that the Negative flag is clear.
   *
   * @param cpu the CPU
   */
  public static void assertNotNegative(W65C02 cpu) {
    if((cpu.p() & W65C02.NEGATIVE) != 0) {
      throw new AssertionError("Negative flag is set.");
    }
  }

  /**
   * Assert that the Zero flag is set.
   *
   * @param cpu the CPU
   */
  public static void assertZero(W65C02 cpu) {
    if((cpu.p() & W65C02.ZERO) == 0) {
      throw new AssertionError("Zero flag is not set.");
    }
  }

  /**
   * Assert that the Zero flag is clear.
   *
   * @param cpu the CPU
   */
  public static void assertNotZero(W65C02 cpu) {
    if((cpu.p() & W65C02.ZERO) != 0) {
      throw new AssertionError("Zero flag is set.");
    }
  }

  /**
   * Assert that the Carry flag is set.
   *
   * @param cpu the CPU
   */
  public static void assertCarry(W65C02 cpu) {
    if((cpu.p() & W65C02.CARRY) == 0) {
      throw new AssertionError("Carry flag is not set.");
    }
  }

  /**
   * Assert that the Carry flag is clear.
   *
   * @param cpu the CPU
   */
  public static void assertNotCarry(W65C02 cpu) {
    if((cpu.p() & W65C02.CARRY) != 0) {
      throw new AssertionError("Carry flag is set.");
    }
  }

  /**
   * Assert that the Overflow flag is set.
   *
   * @param cpu the CPU
   */
  public static void assertOverflow(W65C02 cpu) {
    if((cpu.p() & W65C02.OVERFLOW) == 0) {
      throw new AssertionError("Overflow flag is not set.");
    }
  }

  /**
   * Assert that the Overflow flag is clear.
   *
   * @param cpu the CPU
   */
  public static void assertNotOverflow(W65C02 cpu) {
    if((cpu.p() & W65C02.OVERFLOW) != 0) {
      throw new AssertionError("Overflow flag is set.");
    }
  }

  /**
   * Assert that the Decimal flag is set.
   *
   * @param cpu the CPU
   */
  public static void assertDecimal(W65C02 cpu) {
    if((cpu.p() & W65C02.DECIMAL) == 0) {
      throw new AssertionError("Decimal flag is not set.");
    }
  }

  /**
   * Assert that the Decimal flag is clear.
   *
   * @param cpu the CPU
   */
  public static void assertNotDecimal(W65C02 cpu) {
    if((cpu.p() & W65C02.DECIMAL) != 0) {
      throw new AssertionError("Decimal flag is set.");
    }
  }

  /**
   * Assert that the Interrupt Disable flag is set.
   *
   * @param cpu the CPU
   */
  public static void assertInterruptDisable(W65C02 cpu) {
    if((cpu.p() & W65C02.INTERRUPT_DISABLE) == 0) {
      throw new AssertionError("Interrupt Disable flag is not set.");
    }
  }

  /**
   * Assert that the Interrupt Disable flag is clear.
   *
   * @param cpu the CPU
   */
  public static void assertNotInterruptDisable(W65C02 cpu) {
    if((cpu.p() & W65C02.INTERRUPT_DISABLE) != 0) {
      throw new AssertionError("Interrupt Disable flag is set.");
    }
  }

  /**
   * Return a Consumer that can assert that the A register of a CPU
   * is equal to the given value.
   *
   * @param value the value to assert.
   * @return a Consumer that can evaluate the assertion.
   */
  public static Consumer<W65C02> assertA(int value) {
    return cpu -> assertEquals((byte)(value & 0xFF), cpu.a());
  }

  /**
   * Return a Consumer that can assert that the X register of a CPU
   * is equal to the given value.
   *
   * @param value the value to assert.
   * @return a Consumer that can evaluate the assertion.
   */
  public static Consumer<W65C02> assertX(int value) {
    return cpu -> assertEquals((byte)(value & 0xFF), cpu.x());
  }

  /**
   * Return a Consumer that can assert that the Y register of a CPU
   * is equal to the given value.
   *
   * @param value the value to assert.
   * @return a Consumer that can evaluate the assertion.
   */
  public static Consumer<W65C02> assertY(int value) {
    return cpu -> assertEquals((byte)(value & 0xFF), cpu.y());
  }

  public static Consumer<W65C02> assertMemory(Backplane backplane, int address, int value) {
    return cpu -> {
      cpu.rdy().value(false);
      backplane.address().value(address);
      backplane.rwb().value(true);
      backplane.clock().value(false);
      backplane.clock().value(true);
      cpu.rdy().value(true);

      assertEquals(value, backplane.data().value());
    };
  }
}
