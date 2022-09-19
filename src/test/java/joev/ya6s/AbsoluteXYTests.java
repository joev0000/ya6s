/* Copyright (C) 2021, 2022 Joseph Vigneau */

package joev.ya6s;

import static joev.ya6s.Parameters.params;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class AbsoluteXYTests {
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
    System.out.format("TEST: %s: %s%n", params.name(), params.program());
    TestUtils.executeTest(params, backplane, cpu);
  }

  static Stream<Parameters> tests() {
    return Stream.of(
      params("LDA ,X", """
        A9 23    ; LDA #$23
        8D 60 10 ; STA $1060
        A9 42    ; LDA #$42
        A2 60    ; LDX #$60
        BD 00 10 ; LDA $1000,X
        """, 14,
        Assertions.assertA(0x23)),

      params("STA ,X", """
        A2 08    ; LDX #$08
        A9 23    ; LDA #$23
        9D 88 10 ; STA $1088,X
        A9 42    ; LDA #$42
        AD 90 10 ; LDA $1090
        """, 15,
        Assertions.assertA(0x23)),

      params("ASL", """
        A9 EA    ; LDA #$EA
        8D 46 20 ; STA $2046
        A2 06    ; LDX #$06
        1E 40 20 ; ASL $2040,X
        AD 46 20 ; LDA $2046
        """, 19,
        Assertions::assertCarry,
        Assertions::assertNegative,
        Assertions::assertNotZero,
        Assertions.assertA(0xD4)),

      params("LDX", """
        A9 23    ; LDA #$23
        8D 46 20 ; STA $2046
        A9 42    ; LDA #$42
        A0 06    ; LDY #$06
        BE 40 20 ; LDX $2040,Y
        """, 14,
        Assertions.assertX(0x23))
    );
  }
}


