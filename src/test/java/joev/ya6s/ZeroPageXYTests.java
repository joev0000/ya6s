/* Copyright (C) 2021, 2022 Joseph Vigneau */

package joev.ya6s;

import static joev.ya6s.Parameters.params;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class ZeroPageXYTests {
  private Backplane backplane;
  private W65C02S cpu;

  @BeforeEach
  void beforeEach() {
    backplane = new Backplane();
    cpu = new W65C02S(backplane);
    new SRAM(backplane);
  }

  @ParameterizedTest
  @MethodSource("tests")
  void test(Parameters params) {
    TestUtils.executeTest(params, backplane, cpu);
  }

  static Stream<Parameters> tests() {
    return Stream.of(
      params("LDA", """
        A9 23 ; LDA #$23
        85 88 ; STA $88
        A9 42 ; LDA #$42
        A2 08 ; LDX #$08
        B5 80 ; LDA $80,X
        """, 13,
        Assertions.assertA(0x23)),

      params("STA", """
        A2 08 ; LDX #$08
        A9 23 ; LDA #$23
        95 80 ; STA $80,X
        A9 42 ; LDA #$42
        A5 88 ; LDA $88
        """, 13,
        Assertions.assertA(0x23)),

      params("ASL", """
        A9 EA ; LDA #$EA
        85 46 ; STA $46
        A2 06 ; LDX #$06
        16 40 ; ASL $40,X
        A5 46 ; LDA $46
        """, 16,
        Assertions::assertCarry,
        Assertions::assertNegative,
        Assertions::assertNotZero,
        Assertions.assertA(0xD4)),

      params("LDX", """
        A9 23 ; LDA #$23
        85 46 ; STA $46
        A9 42 ; LDA #$42
        A0 06 ; LDY #$06
        B6 40 ; LDX $40,Y
        """, 13,
        Assertions.assertX(0x23)),

      params("STX", """
        A2 23 ; LDX #$23
        A0 09 ; LDY #$09
        96 40 ; STX $40,Y
        A5 49 ; LDA $49
        """, 11,
        Assertions.assertA(0x23))
    );
  }
}

