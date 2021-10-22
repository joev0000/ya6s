package joev.ya6s;

import static joev.ya6s.Parameters.params;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class ImpliedTests {
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
      params("INC", """
        A9 7F ; LDA #$7F
        1A    ; INC
      """, 4,
      Assertions::assertNegative,
      Assertions::assertNotZero,
      Assertions.assertA(0x80)),

      params("INC", """
        A9 01 ; LDA #$01
        3A    ; DEC
      """, 4,
      Assertions::assertNotNegative,
      Assertions::assertZero,
      Assertions.assertA(0x00)),

      params("ASL", """
        A9 EA ; LDA #$EA
        0A    ; ASL
        """, 4,
        Assertions::assertCarry,
        Assertions::assertNegative,
        Assertions::assertNotZero,
        Assertions.assertA(0xD4)),

      params("ROL", """
        38    ; SEC
        A9 EA ; LDA #$EA
        2A    ; ROL
        """, 6,
        Assertions::assertCarry,
        Assertions::assertNegative,
        Assertions::assertNotZero,
        Assertions.assertA(0xD5)),

      params("LSR", """
        38    ; SEC
        A9 55 ; LDA #$55
        4A    ; LSR
        """, 6,
        Assertions::assertCarry,
        Assertions::assertNotNegative,
        Assertions::assertNotZero,
        Assertions.assertA(0x2A)),

      params("ROR", """
        38    ; SEC
        A9 55 ; LDA #$55
        6A    ; ROR
      """, 6,
      Assertions::assertCarry,
      Assertions::assertNegative,
      Assertions::assertNotZero,
      Assertions.assertA(0xAA)),

      params("DEY", """
        A0 80 ; LDY #$80
        88    ; DEY
        """, 4,
      Assertions::assertNotNegative,
      Assertions::assertNotZero,
      Assertions.assertY(0x7F)),

      params("INY", """
        A0 FF ; LDY #$FF
        C8    ; INY
        """, 4,
        Assertions::assertNotNegative,
        Assertions::assertZero,
        Assertions.assertY(0x00)),

      params("INX", """
        A2 B0 ; LDX #$B0
        E8    ; INX
        """, 4,
        Assertions::assertNegative,
        Assertions::assertNotZero,
        Assertions.assertX(0xB1)),

      params("DEX", """
        A2 B0 ; LDX #$B0
        CA    ; DEX
        """, 4,
        Assertions::assertNegative,
        Assertions::assertNotZero,
        Assertions.assertX(0xAF)),

      params("NOP", "EA ; NOP", 2),

      params("TYA", """
        A0 23 ; LDY #$23
        98    ; TYA
        """, 4,
        Assertions.assertA(0x23)),

      params("TAY", """
        A9 42 ; LDA #$42
        A8    ; TAY
        """, 4,
        Assertions.assertY(0x42)),

      params("TXA", """
        A2 23 ; LDX #$23
        8A    ; TXA
        """, 4,
        Assertions.assertA(0x23)),

      params("TAX", """
        A9 42 ; LDA #$42
        AA    ; TAX
        """, 4,
        Assertions.assertX(0x42)),

      /* TODO: This will have to wait until PHA is implemented.
      params("TXS and TSX", """
        A2 #$FF ; LDX #$FF
        9A      ; TXS
        48      ; PHA
        BA      ; TSX
        """, 9,
        Assertions.assertX(0xFE)),
      */

      params("SEC", """
        38    ; SEC
        """, 2,
        Assertions::assertCarry),

      params("CLC", """
        38    ; SEC
        18    ; CLC
        """, 4,
        Assertions::assertNotCarry),

      params("SEI", """
        78    ; SEI
        """, 2,
        Assertions::assertInterruptDisable),

      params("CLI", """
        78    ; SEI
        58    ; CLI
        """, 4,
        Assertions::assertNotInterruptDisable),

      params("SED", """
        F8    ; SED
        """, 2,
        Assertions::assertDecimal),

      params("CLD", """
        F8    ; SED
        D8    ; CLD
        """, 4,
        Assertions::assertNotDecimal),

      params("CLV", """
        89 40 ; BIT #$40 (sets overflow)
        B8    ; CLV
        """, 4,
        Assertions::assertNotOverflow)
    );
  }
}
