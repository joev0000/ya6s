package joev.ya6s.expression;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import joev.ya6s.Backplane;
import joev.ya6s.SRAM;
import joev.ya6s.W65C02;

public class NotExpressionTests {
  private Backplane backplane;

  @BeforeEach
  public void beforeEach() {
    backplane = new Backplane();
    new SRAM(backplane);
  }

  @Test
  public void sanity() {
    Constant a = new Constant(0x23);
    Constant b = new Constant(0x42);
    RelationalExpression eq = new RelationalExpression(RelationalExpression.Op.EQUALS, a, b);
    NotExpression expr = new NotExpression(eq);
 
    assertTrue(expr.test(new W65C02(backplane)));
  }

  @Test
  public void notNot() {
    Constant a = new Constant(0x23);
    RelationalExpression eq = new RelationalExpression(RelationalExpression.Op.EQUALS, a, a);
    NotExpression not = new NotExpression(eq);
    NotExpression notNot = new NotExpression(not);
    W65C02 cpu = new W65C02(backplane);

    assertFalse(not.test(cpu));
    assertTrue(notNot.test(cpu));
  }
}
