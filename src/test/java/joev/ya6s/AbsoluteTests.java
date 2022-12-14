/* Copyright (C) 2021, 2022 Joseph Vigneau */

package joev.ya6s;

import static joev.ya6s.Parameters.params;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class AbsoluteTests {
  private Backplane backplane;
  private W65C02S cpu;

  @BeforeEach
  void beforeEach() {
    backplane = new Backplane();
    cpu = new W65C02S(backplane);
    new SRAM(backplane);

    TestUtils.load(backplane, cpu, 0x3000, """
      A9 42 ; LDA #$42
      DB    ; STP
    """);

    TestUtils.load(backplane, cpu, 0x4000, """
      A9 42 ; LDA #$42
      60    ; RTS
    """);
  }

  @ParameterizedTest
  @MethodSource("tests")
  void test(Parameters params) {
    TestUtils.executeTest(params, backplane, cpu);
  }

  static Stream<Parameters> tests() {
    return Stream.of(
      params("STA and LDA", """
        A9 23    ; LDA #$23
        8D 00 10 ; STA $1000
        A9 42    ; LDA #$42
        AD 00 10 ; LDA $1000
        """, 12,
        Assertions::assertNotNegative,
        Assertions::assertNotZero,
        Assertions.assertA(0x23)),

      params("STX and LDX", """
        A2 FF    ; LDX #$FF
        8E 40 20 ; STX $2040
        A2 00    ; LDX #$00
        AE 40 20 ; LDX $2040
        """, 12,
        Assertions::assertNegative,
        Assertions::assertNotZero,
        Assertions.assertX(0xFF)),

      params("STY and LDY", """
        A0 FF    ; LDY #$FF
        8C 40 20 ; STY $2040
        A0 00    ; LDY #$00
        AC 40 20 ; LDY $2040
        """, 12,
        Assertions::assertNegative,
        Assertions::assertNotZero,
        Assertions.assertY(0xFF)),

      params("ASL", """
        A9 EA    ; LDA #$EA
        8D 00 10 ; STA $1000
        0E 00 10 ; ASL $1000
        AD 00 10 ; LDA $1000
        """, 16,
        Assertions::assertCarry,
        Assertions::assertNegative,
        Assertions::assertNotZero,
        Assertions.assertA(0xD4)),

      params("ROL", """
        38       ; SEC
        A9 EA    ; LDA #$EA
        8D 00 10 ; STA $1000
        2E 00 10 ; ROL $1000
        AD 00 10 ; LDA $1000
        """, 18,
        Assertions::assertCarry,
        Assertions::assertNegative,
        Assertions::assertNotZero,
        Assertions.assertA(0xD5)),

      params("LSR", """
        38
        A9 55    ; LDA #$55
        8D 00 10 ; STA $1000
        4E 00 10 ; LSR $1000
        AD 00 10 ; LDA $1000
        """, 18,
        Assertions::assertCarry,
        Assertions::assertNotNegative,
        Assertions::assertNotZero,
        Assertions.assertA(0x2A)),

      params("ROR", """
        38
        A9 55    ; LDA #$55
        8D 00 10 ; STA $1000
        6E 00 10 ; ROR $1000
        AD 00 10 ; LDA $1000
      """, 18,
      Assertions::assertCarry,
      Assertions::assertNegative,
      Assertions::assertNotZero,
      Assertions.assertA(0xAA)),

      params("JMP", """
        4C 00 30 ; JMP $3000
        A9 23    ; LDA #$23
        """, 5,
        Assertions.assertA(0x42)),

      params("JSR", """
        20 00 40 ; JSR $4000
        A2 23    ; LDX #$23
        """, 16,
        Assertions.assertX(0x23),
        Assertions.assertA(0x42))
    );
  }
}
