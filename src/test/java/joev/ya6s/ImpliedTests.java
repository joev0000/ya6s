package joev.ya6s;

import static joev.ya6s.Parameters.params;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class ImpliedTests {
  private W65C02 cpu;

  @BeforeEach
  void beforeEach() {
    cpu = new W65C02();
  }

  @ParameterizedTest
  @MethodSource("tests")
  void test(Parameters params) {
    TestUtils.executeTest(params, cpu);
  }

  static Stream<Parameters> tests() {
    return Stream.of(
        params("DEY", "A0 01 88", 4,
          Assertions::assertZero,
          Assertions::assertNotNegative,
          Assertions.assertY(0)),

        params("INY", "A0 7F C8", 4,
          Assertions::assertNotZero,
          Assertions::assertNegative,
          Assertions.assertY(0x80)),

        params("DEX", "A2 01 CA", 4,
          Assertions::assertZero,
          Assertions::assertNotNegative,
          Assertions.assertX(0)),

        params("INX", "A2 7F E8", 4,
          Assertions::assertNotZero,
          Assertions::assertNegative,
          Assertions.assertX(0x80)),

        params("TAY", "A9 23 A8", 4,
          Assertions.assertY(0x23)),

        params("TYA", "A0 42 98", 4,
          Assertions.assertA(0x42)),

        params("TAX", "A9 23 AA", 4,
          Assertions.assertX(0x23)),

        params("TXA", "A2 42 8A", 4,
          Assertions.assertA(0x42)),

        /* TODO: need stack instructions
        params("TSX", "", 4,
          Assertions.assertX(0x23)),

        params("TXS", "A2 42 8A", 4,
          Assertions.assertA(0x42)),
        */

        params("NOP", "EA", 2),

        params("CLC", "38 18", 4,
          Assertions::assertNotCarry),

        params("SEC", "18 38", 4,
          Assertions::assertCarry),

        params("CLD", "F8 D8", 4,
          Assertions::assertNotDecimal),

        params("SED", "D8 F8", 4,
          Assertions::assertDecimal),

        params("CLI", "78 58", 4,
          Assertions::assertNotInterruptDisable),

        params("SEI", "58 78", 4,
          Assertions::assertInterruptDisable),

        // BIT sets overflow flag if bit 6 is set.
        params("CLV", "89 40 B8", 4,
            Assertions::assertNotOverflow),

        params("INC", "A9 FF 1A", 4,
            Assertions::assertZero,
            Assertions::assertNotNegative,
            Assertions.assertA(0)),

        params("DEC", "A9 00 3A", 4,
            Assertions::assertNotZero,
            Assertions::assertNegative,
            Assertions.assertA(0xFF)),

        params("ASL", "A9 AA 0A", 4,
            Assertions::assertCarry,
            Assertions::assertNotNegative,
            Assertions.assertA(0x54)),

        params("ROL", "A9 AA 38 2A", 6,
            Assertions::assertCarry,
            Assertions::assertNotNegative,
            Assertions.assertA(0x55)),

        params("LSR", "A9 01 38 4A", 6,
            Assertions::assertCarry,
            Assertions::assertNotNegative,
            Assertions.assertA(0)),

        params("ROR", "A9 01 38 6A", 6,
            Assertions::assertCarry,
            Assertions::assertNegative,
            Assertions.assertA(0x80)),

        params("placeholder", "A0 23 88", 4,
          Assertions.assertY(0x22))
    );
  }
}

