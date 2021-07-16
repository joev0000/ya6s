package joev.ya6s.expression;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static joev.ya6s.Parameters.params;
import static joev.ya6s.TestUtils.executeTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import joev.ya6s.Backplane;
import joev.ya6s.SRAM;

public class RelationalExpressionTests {
  private Backplane backplane;

  @BeforeEach
  public void beforeEach() {
    backplane = new Backplane();
    new SRAM(backplane);
  }

  @Test
  public void equalsRegisterConstantTrue() {
    Register a = Register.A;
    Constant b = new Constant(0x23);
    RelationalExpression expr = new RelationalExpression(RelationalExpression.Op.EQUALS, a, b);

    executeTest(params(
      "A", "A9 23", 2,
      cpu -> assertTrue(expr.test(cpu))));
  }

  @Test
  public void equalsRegisterConstantFalse() {
    Register a = Register.A;
    Constant b = new Constant(0x42);
    RelationalExpression expr = new RelationalExpression(RelationalExpression.Op.EQUALS, a, b);

    executeTest(params(
      "A", "A9 23", 2,
      cpu -> assertFalse(expr.test(cpu))));
  }

  @Test
  public void notEqualsRegisterConstantTrue() {
    Register a = Register.A;
    Constant b = new Constant(0x42);
    RelationalExpression expr = new RelationalExpression(RelationalExpression.Op.NOT_EQUALS, a, b);

    executeTest(params(
      "A", "A9 23", 2,
      cpu -> assertTrue(expr.test(cpu))));
  }

  @Test
  public void notEqualsRegisterConstantFalse() {
    Register a = Register.A;
    Constant b = new Constant(0x23);
    RelationalExpression expr = new RelationalExpression(RelationalExpression.Op.NOT_EQUALS, a, b);

    executeTest(params(
      "A", "A9 23", 2,
      cpu -> assertFalse(expr.test(cpu))));
  }

  @Test
  public void greaterThanRegisterConstantTrue() {
    Register a = Register.A;
    Constant b = new Constant(0x05);
    RelationalExpression expr = new RelationalExpression(RelationalExpression.Op.GREATER_THAN, a, b);

    executeTest(params(
      "A", "A9 23", 2,
      cpu -> assertTrue(expr.test(cpu))));
  }

  @Test
  public void greaterThanRegisterConstantFalse() {
    Register a = Register.A;
    Constant b = new Constant(0x42);
    RelationalExpression expr = new RelationalExpression(RelationalExpression.Op.GREATER_THAN, a, b);

    executeTest(params(
      "A", "A9 23", 2,
      cpu -> assertFalse(expr.test(cpu))));
  }

  @Test
  public void lessThanRegisterConstantTrue() {
    Register a = Register.A;
    Constant b = new Constant(0x42);
    RelationalExpression expr = new RelationalExpression(RelationalExpression.Op.LESS_THAN, a, b);

    executeTest(params(
      "A", "A9 23", 2,
      cpu -> assertTrue(expr.test(cpu))));
  }

  @Test
  public void lessThanRegisterConstantFalse() {
    Register a = Register.A;
    Constant b = new Constant(0x05);
    RelationalExpression expr = new RelationalExpression(RelationalExpression.Op.LESS_THAN, a, b);

    executeTest(params(
      "A", "A9 23", 2,
      cpu -> assertFalse(expr.test(cpu))));
  }
}
