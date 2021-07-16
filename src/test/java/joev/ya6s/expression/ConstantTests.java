package joev.ya6s.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;

import joev.ya6s.Backplane;
import joev.ya6s.W65C02;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for constant functions.
 */
public class ConstantTests {
  private Backplane backplane;
  private W65C02 cpu;

  @BeforeEach
  void beforeEach() {
    backplane = new Backplane();
    cpu = new W65C02(backplane);
  }

  @Test
  void basic() {
    Constant c = new Constant(Integer.valueOf(23));

    assertEquals(Integer.valueOf(23), c.apply(cpu));
  }
}
