/* Copyright (C) 2021, 2022 Joseph Vigneau */

package joev.ya6s.expression;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static joev.ya6s.Parameters.params;
import static joev.ya6s.TestUtils.executeTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import joev.ya6s.Backplane;
import joev.ya6s.SRAM;

public class LogicExpressionTests {
  private Backplane backplane;

  private Constant c23 = new Constant(0x23);
  private Constant c42 = new Constant(0x42);
  private RelationalExpression aEq32 = new RelationalExpression(RelationalExpression.Op.EQUALS, Register.A, c23);
  private RelationalExpression xEq42 = new RelationalExpression(RelationalExpression.Op.EQUALS, Register.X, c42);

  @BeforeEach
  public void beforeEach() {
    backplane = new Backplane();
    new SRAM(backplane);
  }

  @Test
  public void andTT() {
    LogicExpression expr = new LogicExpression(LogicExpression.Op.AND, aEq32, xEq42);

    executeTest(params(
      "AND T T", "A9 23 A2 42", 4,
      cpu -> assertTrue(expr.test(cpu))));
  }

  @Test
  public void andTF() {
    LogicExpression expr = new LogicExpression(LogicExpression.Op.AND, aEq32, xEq42);

    executeTest(params(
      "AND T F", "A9 23 A2 69", 4,
      cpu -> assertFalse(expr.test(cpu))));
  }

  @Test
  public void andFT() {
    LogicExpression expr = new LogicExpression(LogicExpression.Op.AND, aEq32, xEq42);

    executeTest(params(
      "AND F T", "A9 69 A2 42", 4,
      cpu -> assertFalse(expr.test(cpu))));
  }

  @Test
  public void andFF() {
    LogicExpression expr = new LogicExpression(LogicExpression.Op.AND, aEq32, xEq42);

    executeTest(params(
      "AND F F", "A9 69 A2 69", 4,
      cpu -> assertFalse(expr.test(cpu))));
  }

  @Test
  public void orTT() {
    LogicExpression expr = new LogicExpression(LogicExpression.Op.OR, aEq32, xEq42);

    executeTest(params(
      "OR T T", "A9 23 A2 42", 4,
      cpu -> assertTrue(expr.test(cpu))));
  }

  @Test
  public void orTF() {
    LogicExpression expr = new LogicExpression(LogicExpression.Op.OR, aEq32, xEq42);

    executeTest(params(
      "OR T F", "A9 23 A2 69", 4,
      cpu -> assertTrue(expr.test(cpu))));
  }

  @Test
  public void orFT() {
    LogicExpression expr = new LogicExpression(LogicExpression.Op.OR, aEq32, xEq42);

    executeTest(params(
      "OR F T", "A9 69 A2 42", 4,
      cpu -> assertTrue(expr.test(cpu))));
  }

  @Test
  public void orFF() {
    LogicExpression expr = new LogicExpression(LogicExpression.Op.OR, aEq32, xEq42);

    executeTest(params(
      "OR F F", "A9 69 A2 69", 4,
      cpu -> assertFalse(expr.test(cpu))));
  }

  @Test
  public void xorTT() {
    LogicExpression expr = new LogicExpression(LogicExpression.Op.XOR, aEq32, xEq42);

    executeTest(params(
      "XOR T T", "A9 23 A2 42", 4,
      cpu -> assertFalse(expr.test(cpu))));
  }

  @Test
  public void xorTF() {
    LogicExpression expr = new LogicExpression(LogicExpression.Op.XOR, aEq32, xEq42);

    executeTest(params(
      "XOR T F", "A9 23 A2 69", 4,
      cpu -> assertTrue(expr.test(cpu))));
  }

  @Test
  public void xorFT() {
    LogicExpression expr = new LogicExpression(LogicExpression.Op.XOR, aEq32, xEq42);

    executeTest(params(
      "XOR F T", "A9 69 A2 42", 4,
      cpu -> assertTrue(expr.test(cpu))));
  }

  @Test
  public void xorFF() {
    LogicExpression expr = new LogicExpression(LogicExpression.Op.XOR, aEq32, xEq42);

    executeTest(params(
      "XOR F F", "A9 69 A2 69", 4,
      cpu -> assertFalse(expr.test(cpu))));
  }
}
