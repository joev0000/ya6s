/* Copyright (C) 2021, 2022 Joseph Vigneau */

package joev.ya6s.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static joev.ya6s.Parameters.params;
import static joev.ya6s.TestUtils.executeTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import joev.ya6s.Backplane;
import joev.ya6s.SRAM;

public class ArithmeticExpressionTests {
  private Backplane backplane;

  @BeforeEach
  public void beforeEach() {
    backplane = new Backplane();
    new SRAM(backplane);
  }

  @Test
  public void addRegisterConstant() {
    Register a = Register.A;
    Constant b = new Constant(5);
    ArithmeticExpression expr = new ArithmeticExpression(ArithmeticExpression.Op.ADD, a, b);

    executeTest(params(
      "A", "A9 23", 2,
      cpu -> assertEquals(0x23 + 5, expr.apply(cpu))));
  }

  @Test
  public void addRegisterRegister() {
    Register a = Register.A;
    Register b = Register.X;
    ArithmeticExpression expr = new ArithmeticExpression(ArithmeticExpression.Op.ADD, a, b);

    executeTest(params(
      "A", "A9 23 A2 05", 4,
      cpu -> assertEquals(0x23 + 0x05, expr.apply(cpu))));
  }

  @Test
  public void addRegisterRegsiterRegister() {
    Register a = Register.A;
    Register b = Register.X;
    Register c = Register.Y;
    ArithmeticExpression aPlusB = new ArithmeticExpression(ArithmeticExpression.Op.ADD, a, b);
    ArithmeticExpression aPlusBPlusC = new ArithmeticExpression(ArithmeticExpression.Op.ADD, aPlusB, c);

    executeTest(params(
      "A", "A9 23 A2 05 A0 42", 6,
      cpu -> assertEquals(0x23 + 0x05 + 0x42, aPlusBPlusC.apply(cpu))));
  }

  @Test
  public void subRegisterConstant() {
    Register a = Register.A;
    Constant b = new Constant(5);
    ArithmeticExpression expr = new ArithmeticExpression(ArithmeticExpression.Op.SUB, a, b);

    executeTest(params(
      "A", "A9 23", 2,
      cpu -> assertEquals(0x23 - 5, expr.apply(cpu))));
  }

  @Test
  public void andRegisterConstant() {
    Register a = Register.A;
    Constant b = new Constant(0x62);
    ArithmeticExpression expr = new ArithmeticExpression(ArithmeticExpression.Op.AND, a, b);

    executeTest(params(
      "A", "A9 23", 2,
      cpu -> assertEquals(0x23 & 0x62, expr.apply(cpu))));
  }

  @Test
  public void orRegisterConstant() {
    Register a = Register.A;
    Constant b = new Constant(0x62);
    ArithmeticExpression expr = new ArithmeticExpression(ArithmeticExpression.Op.OR, a, b);

    executeTest(params(
      "A", "A9 23", 2,
      cpu -> assertEquals(0x23 | 0x62, expr.apply(cpu))));
  }

  @Test
  public void xorRegisterConstant() {
    Register a = Register.A;
    Constant b = new Constant(0x62);
    ArithmeticExpression expr = new ArithmeticExpression(ArithmeticExpression.Op.XOR, a, b);

    executeTest(params(
      "A", "A9 23", 2,
      cpu -> assertEquals(0x23 ^ 0x62, expr.apply(cpu))));
  }

  @Test
  public void mulRegisterConstant() {
    Register a = Register.A;
    Constant b = new Constant(0x05);
    ArithmeticExpression expr = new ArithmeticExpression(ArithmeticExpression.Op.MUL, a, b);

    executeTest(params(
      "A", "A9 23", 2,
      cpu -> assertEquals(0x23 * 0x05, expr.apply(cpu))));
  }

  @Test
  public void divRegisterConstant() {
    Register a = Register.A;
    Constant b = new Constant(0x05);
    ArithmeticExpression expr = new ArithmeticExpression(ArithmeticExpression.Op.DIV, a, b);

    executeTest(params(
      "A", "A9 23", 2,
      cpu -> assertEquals(0x23 / 0x05, expr.apply(cpu))));
  }

  @Test
  public void modRegisterConstant() {
    Register a = Register.A;
    Constant b = new Constant(0x05);
    ArithmeticExpression expr = new ArithmeticExpression(ArithmeticExpression.Op.MOD, a, b);

    executeTest(params(
      "A", "A9 23", 2,
      cpu -> assertEquals(0x23 % 0x05, expr.apply(cpu))));
  }
}
