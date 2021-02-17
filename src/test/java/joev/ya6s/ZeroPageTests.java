package joev.ya6s;

import static joev.ya6s.Parameters.params;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class ZeroPageTests {
  private Backplane backplane;
  private W65C02 cpu;
  private SRAM ram;

  @BeforeEach
  void beforeEach() {
    backplane = new Backplane();
    cpu = new W65C02(backplane);
    ram = new SRAM(backplane);
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
        85 40    ; STA $40
        A9 42    ; LDA #$42
        A5 40    ; LDA $40
        """, 10,
        Assertions.assertA(0x23)),

      params("STX and LDX", """
        A2 23    ; LDX #$23
        86 40    ; STX $40
        A2 42    ; LDX #$42
        A6 40    ; LDX $40
        """, 10,
        Assertions.assertX(0x23)),

      params("STY and LDY", """
        A0 23    ; LDY #$23
        84 40    ; STY $40
        A0 42    ; LDY #$42
        A4 40    ; LDY $40
        """, 10,
        Assertions.assertY(0x23)),

      params("BIT zero", """
        A9 3C ; LDA #$3C  00111100
        85 40 ; STA $40
        A9 C3 ; LDA #$C3  11000011
        24 40 ; BIT $40   00111100
        """, 10,
        Assertions::assertNotNegative,
        Assertions::assertZero,
        Assertions::assertNotOverflow,
        Assertions.assertA(0xC3)),

      params("BIT negative", """
        A9 B0 ; LDA #$B0
        85 40 ; STA $40
        A9 03 ; LDA #$03
        24 40 ; BIT $40
        """, 10,
        Assertions::assertNegative,
        Assertions::assertZero,
        Assertions::assertNotOverflow,
        Assertions.assertA(0x03)),

      params("BIT overflow", """
        A9 40 ; LDA #$40
        85 40 ; STA $40
        A9 03 ; LDA #$03
        24 40 ; BIT $40
        """, 10,
        Assertions::assertNotNegative,
        Assertions::assertZero,
        Assertions::assertOverflow,
        Assertions.assertA(0x03)),

      params("ASL", """
        A9 EA    ; LDA #$EA
        85 10    ; STA $10
        06 10    ; ASL $10
        A5 10    ; LDA $10
        """, 13,
        Assertions::assertCarry,
        Assertions::assertNegative,
        Assertions::assertNotZero,
        Assertions.assertA(0xD4)),

      params("ROL", """
        38       ; SEC
        A9 EA    ; LDA #$EA
        85 10    ; STA $10
        26 10    ; ROL $10
        A5 10    ; LDA $10
        """, 15,
        Assertions::assertCarry,
        Assertions::assertNegative,
        Assertions::assertNotZero,
        Assertions.assertA(0xD5)),

      params("LSR", """
        38
        A9 55    ; LDA #$55
        85 10    ; STA $10
        46 10    ; LSR $10
        A5 10    ; LDA $10
        """, 15,
        Assertions::assertCarry,
        Assertions::assertNotNegative,
        Assertions::assertNotZero,
        Assertions.assertA(0x2A)),

      params("ROR", """
        38
        A9 55    ; LDA #$55
        85 10    ; STA $10
        66 10    ; ROR $10
        A5 10    ; LDA $10
      """, 15,
      Assertions::assertCarry,
      Assertions::assertNegative,
      Assertions::assertNotZero,
      Assertions.assertA(0xAA)),

      params("SMB5", """
        64 10 ; STZ  $10
        D7 10 ; SMB5 $10
        A5 10 ; LDA  $10
        """, 11,
        Assertions.assertA(0b00100000)),

      params("RMB6", """
        A9 FF ; LDA  #$FF
        85 10 ; STA  $10
        67 10 ; RMB6 $10
        A5 10 ; LDA  $10
        """, 13,
        Assertions.assertA(0b10111111))
    );
  }
}
