package joev.ya6s;

import static joev.ya6s.Parameters.params;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;


public class ImmediateTests {
  @ParameterizedTest
  @MethodSource("tests")
  void test(Parameters params) {
    TestUtils.executeTest(params);
  }

  static Stream<Parameters> tests() {
    return Stream.of(
      params("AND", """
        A9 C3 ; LDA #$C3  11000011
        29 7E ; AND #$7E  01111110
        """, 4,
        Assertions::assertNotNegative,
        Assertions::assertNotZero,
        Assertions.assertA(0x42)),
      params("AND negative", """
        A9 C3 ; LDA #$C3  11000011
        29 BD ; AND #$BD  10111101
        """, 4,
        Assertions::assertNegative,
        Assertions::assertNotZero,
        Assertions.assertA(0x81)),
      params("AND zero", """
        A9 C3 ; LDA #$C3  11000011
        29 3C ; AND #$3C  00111100
        """, 4,
        Assertions::assertNotNegative,
        Assertions::assertZero,
        Assertions.assertA(0x00)),

      params("BIT zero immediate does not modify NV", """
        A9 C3 ; LDA #$C3  11000011
        89 3C ; BIT #$3C  00111100
        """, 4,
        Assertions::assertNegative,
        Assertions::assertZero,
        Assertions::assertNotOverflow,
        Assertions.assertA(0xC3)),
      params("BIT negative immediate does not modify NV", """
        A9 03 ; LDA #$03
        89 B0 ; BIT #$B0
        """, 4,
        Assertions::assertNotNegative,
        Assertions::assertZero,
        Assertions::assertNotOverflow,
        Assertions.assertA(0x03)),
      params("BIT overflow immediate does not modify NV", """
        A9 03 ; LDA #$03
        89 40 ; BIT #$40
        """, 4,
        Assertions::assertNotNegative,
        Assertions::assertZero,
        Assertions::assertNotOverflow,
        Assertions.assertA(0x03)),

      params("CMP equal", """
        A9 23 ; LDA #$23
        C9 23 ; CMP #$23
        """, 4,
        Assertions::assertZero,
        Assertions::assertCarry,
        Assertions::assertNotNegative,
        Assertions.assertA(0x23)),
      params("CMP greater than", """
        A9 23 ; LDA #$23
        C9 24 ; CMP #$24
        """, 4,
        Assertions::assertNotZero,
        Assertions::assertNotCarry,
        Assertions::assertNegative,
        Assertions.assertA(0x23)),
      params("CMP less than", """
        A9 23 ; LDA #$23
        C9 22 ; CMP #$22
        """, 4,
        Assertions::assertNotZero,
        Assertions::assertCarry,
        Assertions::assertNotNegative,
        Assertions.assertA(0x23)),

      params("CPX equal", """
        A2 23 ; LDX #$23
        E0 23 ; CPX #$23
        """, 4,
        Assertions::assertZero,
        Assertions::assertCarry,
        Assertions::assertNotNegative,
        Assertions.assertX(0x23)),
      params("CPX greater than", """
        A2 23 ; LDX #$23
        E0 24 ; CPX #$24
        """, 4,
        Assertions::assertNotZero,
        Assertions::assertNotCarry,
        Assertions::assertNegative,
        Assertions.assertX(0x23)),
      params("CPX less than", """
        A2 23 ; LDX #$23
        E0 22 ; CPX #$22
        """, 4,
        Assertions::assertNotZero,
        Assertions::assertCarry,
        Assertions::assertNotNegative,
        Assertions.assertX(0x23)),

      params("CPY equal", """
        A0 23 ; LDY #$23
        C0 23 ; CPY #$23
        """, 4,
        Assertions::assertZero,
        Assertions::assertCarry,
        Assertions::assertNotNegative,
        Assertions.assertY(0x23)),
      params("CPY greater than", """
        A0 23 ; LDY #$23
        C0 24 ; CPY #$24
        """, 4,
        Assertions::assertNotZero,
        Assertions::assertNotCarry,
        Assertions::assertNegative,
        Assertions.assertY(0x23)),
      params("CPY less than", """
        A0 23 ; LDY #$23
        C0 22 ; CPY #$22
        """, 4,
        Assertions::assertNotZero,
        Assertions::assertCarry,
        Assertions::assertNotNegative,
        Assertions.assertY(0x23)),

      params("EOR", """
        A9 C3 ; LDA #$C3  11000011
        49 FE ; EOR #$FE  11111110
        """, 4,
        Assertions::assertNotNegative,
        Assertions::assertNotZero,
        Assertions.assertA(0x3D)),
      params("EOR negative", """
        A9 C3 ; LDA #$C3  11000011
        49 3D ; EOR #$3D  00111101
        """, 4,
        Assertions::assertNegative,
        Assertions::assertNotZero,
        Assertions.assertA(0xFE)),
      params("EOR zero", """
        A9 C3 ; LDA #$C3  11000011
        49 C3 ; EOR #$C3  11000011
        """, 4,
        Assertions::assertNotNegative,
        Assertions::assertZero,
        Assertions.assertA(0x00)),

      params("LDA", "A9 23 ; LDA #$23", 2,
        Assertions::assertNotNegative,
        Assertions::assertNotZero,
        Assertions.assertA(0x23)),
      params("LDA negative", "A9 AA ; LDA #$AA", 2,
        Assertions::assertNegative,
        Assertions::assertNotZero,
        Assertions.assertA(0xAA)),
      params("LDA zero", "A9 00 ; LDA #$00", 2,
        Assertions::assertNotNegative,
        Assertions::assertZero,
        Assertions.assertA(0x00)),

      params("LDX", "A2 23 ; LDX #$23", 2,
        Assertions::assertNotNegative,
        Assertions::assertNotZero,
        Assertions.assertX(0x23)),
      params("LDX negative", "A2 AA ; LDX #$AA", 2,
        Assertions::assertNegative,
        Assertions::assertNotZero,
        Assertions.assertX(0xAA)),
      params("LDX zero", "A2 00 ; LDX #$00", 2,
        Assertions::assertNotNegative,
        Assertions::assertZero,
        Assertions.assertX(0x00)),

      params("LDY", "A0 23 ; LDY #$23", 2,
        Assertions::assertNotNegative,
        Assertions::assertNotZero,
        Assertions.assertY(0x23)),
      params("LDY negative", "A0 AA ; LDY #$AA", 2,
        Assertions::assertNegative,
        Assertions::assertNotZero,
        Assertions.assertY(0xAA)),
      params("LDY zero", "A0 00 ; LDY #$00", 2,
        Assertions::assertNotNegative,
        Assertions::assertZero,
        Assertions.assertY(0x00)),

      params("ORA", """
        A9 43 ; LDA #$43  01000011
        09 19 ; ORA #$19  00011001
        """, 4,
        Assertions::assertNotNegative,
        Assertions::assertNotZero,
        Assertions.assertA(0x5B)),
      params("ORA negative", """
        A9 C3 ; LDA #$C3  11000011
        09 3D ; ORA #$3D  00111101
        """, 4,
        Assertions::assertNegative,
        Assertions::assertNotZero,
        Assertions.assertA(0xFF)),
      params("ORA zero", """
        A9 00 ; LDA #$C3  00000000
        09 00 ; ORA #$C3  00000000
        """, 4,
        Assertions::assertNotNegative,
        Assertions::assertZero,
        Assertions.assertA(0x00)),

      params("ADC no carry-in", """
        18    ; CLC
        A9 01 ; LDA #$01
        69 02 ; ADC #$02
        """, 6,
        Assertions::assertNotNegative,
        Assertions::assertNotZero,
        Assertions::assertNotCarry,
        Assertions::assertNotOverflow,
        Assertions.assertA(0x03)),
      params("ADC with carry-in", """
        38    ; SEC
        A9 01 ; LDA #$01
        69 02 ; ADC #$02
        """, 6,
        Assertions::assertNotNegative,
        Assertions::assertNotZero,
        Assertions::assertNotCarry,
        Assertions::assertNotOverflow,
        Assertions.assertA(0x04)),
      params("ADC negative and overflow", """
        18    ; CLC
        A9 01 ; LDA #$01
        69 7F ; ADC #$7F
        """, 6,
        Assertions::assertNegative,
        Assertions::assertNotZero,
        Assertions::assertNotCarry,
        Assertions::assertOverflow,
        Assertions.assertA(0x80)),
      params("ADC non-negative and overflow", """
        18    ; CLC
        A9 FF ; LDA #$FF
        69 80 ; ADC #$80
        """, 6,
        Assertions::assertNotNegative,
        Assertions::assertNotZero,
        Assertions::assertCarry,
        Assertions::assertOverflow,
        Assertions.assertA(0x7F)),
      params("ADC carry and zero", """
        18    ; CLC
        A9 DD ; LDA #$DD
        69 23 ; ADC #$23
        """, 6,
        Assertions::assertNotNegative,
        Assertions::assertZero,
        Assertions::assertCarry,
        Assertions::assertNotOverflow,
        Assertions.assertA(0x00)),

      params("SBC with borrow", """
        18    ; CLC
        A9 05 ; LDA #$05
        E9 02 ; SBC #$02
        """, 6,
        Assertions::assertNotNegative,
        Assertions::assertNotZero,
        Assertions::assertCarry,
        Assertions::assertNotOverflow,
        Assertions.assertA(0x02)),
      params("SBC without borrow", """
        38    ; SEC
        A9 05 ; LDA #$05
        E9 02 ; SBC #$02
        """, 6,
        Assertions::assertNotNegative,
        Assertions::assertNotZero,
        Assertions::assertCarry,
        Assertions::assertNotOverflow,
        Assertions.assertA(0x03))
    );
  }
}
