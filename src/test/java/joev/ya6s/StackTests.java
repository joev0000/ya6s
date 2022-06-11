/* Copyright (C) 2021, 2022 Joseph Vigneau */

package joev.ya6s;

import static joev.ya6s.Parameters.params;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class StackTests {
  private Backplane backplane;
  private W65C02S cpu;
  private SRAM ram;

  @BeforeEach
  void beforeEach() {
    backplane = new Backplane();
    cpu = new W65C02S(backplane);
    ram = new SRAM(backplane);
  }

  @ParameterizedTest
  @MethodSource("tests")
  void test(Parameters params) {
    TestUtils.executeTest(params, backplane, cpu);
  }

  static Stream<Parameters> tests() {
    return Stream.of(
      params("PHA and PLA", """
        A9 23 ; LDA #$23
        48    ; PHA
        A9 42 ; LDA #$42
        68    ; PLA
        """, 11,
        Assertions.assertA(0x23)),

      params("PHX and PLX", """
        A2 23 ; LDX #$23
        DA    ; PHX
        A2 42 ; LDX #$42
        FA    ; PLX
        """, 11,
        Assertions.assertX(0x23)),

      params("PHY and PLY", """
        A0 23 ; LDY #$23
        5A    ; PHY
        A0 42 ; LDY #$42
        7A    ; PLY
        """, 11,
        Assertions.assertY(0x23)),

      params("PHP and PLP", """
        38 ; SEC
        08 ; PHP
        18 ; CLC
        28 ; PLP
        """, 11,
        Assertions::assertCarry),

      params("PLP 0", """
        A9 00 ; LDA #$00
        38    ; SEC
        48    ; PHA
        28    ; PLP
        """, 11,
        Assertions::assertNotCarry,
        Assertions::assertNotZero,
        Assertions::assertNotDecimal,
        Assertions::assertNotOverflow,
        Assertions::assertNotNegative,
        Assertions::assertNotInterruptDisable)
    );
  }
}
