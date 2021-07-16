package joev.ya6s.expression;

import static joev.ya6s.Parameters.params;
import static joev.ya6s.TestUtils.executeTest;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import joev.ya6s.Backplane;
import joev.ya6s.SRAM;

public class RegisterTests {
  private Backplane backplane;

  @BeforeEach
  public void beforeEach() {
    backplane = new Backplane();
    new SRAM(backplane);
  }

  @Test
  @Disabled("Test not yet implemented.")
  public void pc() {
  }

  @Test
  public void a() {
    Register reg = Register.A;
    executeTest(params(
      "A", "A9 23", 2,
      cpu -> assertEquals(0x23, reg.apply(cpu))));
  }

  @Test
  public void x() {
    Register reg = Register.X;
    executeTest(params(
      "X", "A2 42", 2,
      cpu -> assertEquals(0x42, reg.apply(cpu))));
  }

  @Test
  public void y() {
    Register reg = Register.Y;
    executeTest(params(
      "Y", "A0 69", 2,
      cpu -> assertEquals(0x69, reg.apply(cpu))));
  }

  @Test
  @Disabled("Test not yet implemented.")
  public void s() {
  }

  @Test
  public void c0() {
    Register reg = Register.C;
    executeTest(params(
      "C 0", "18", 2,
      cpu -> assertEquals(0, reg.apply(cpu))));
  }

  @Test
  public void c1() {
    Register reg = Register.C;
    executeTest(params(
      "C 1", "38", 2,
      cpu -> assertEquals(1, reg.apply(cpu))));
  }

  @Test
  public void z0() {
    Register reg = Register.Z;
    executeTest(params(
      "Z 0", "A9 23", 2,
      cpu -> assertEquals(0, reg.apply(cpu))));
  }

  @Test
  public void z1() {
    Register reg = Register.Z;
    executeTest(params(
      "Z 0", "A9 00", 2,
      cpu -> assertEquals(1, reg.apply(cpu))));
  }

  @Test
  public void n0() {
    Register reg = Register.N;
    executeTest(params(
      "Z 0", "A9 23", 2,
      cpu -> assertEquals(0, reg.apply(cpu))));
  }

  @Test
  public void n1() {
    Register reg = Register.N;
    executeTest(params(
      "Z 0", "A9 80", 2,
      cpu -> assertEquals(1, reg.apply(cpu))));
  }

  @Test
  @Disabled("Test not yet implemented.")
  public void v0() {
  }

  @Test
  @Disabled("Test not yet implemented.")
  public void v1() {
  }

  @Test
  public void i0() {
    Register reg = Register.I;
    executeTest(params(
      "D 0", "58", 2,
      cpu -> assertEquals(0, reg.apply(cpu))));
  }

  @Test
  public void i1() {
    Register reg = Register.I;
    executeTest(params(
      "D 1", "78", 2,
      cpu -> assertEquals(1, reg.apply(cpu))));
  }

  @Test
  public void d0() {
    Register reg = Register.D;
    executeTest(params(
      "D 0", "D8", 2,
      cpu -> assertEquals(0, reg.apply(cpu))));
  }

  @Test
  public void d1() {
    Register reg = Register.D;
    executeTest(params(
      "D 1", "F8", 2,
      cpu -> assertEquals(1, reg.apply(cpu))));
  }
}
