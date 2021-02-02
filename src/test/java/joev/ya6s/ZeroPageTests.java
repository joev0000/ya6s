package joev.ya6s;

import static joev.ya6s.Parameters.params;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class ZeroPageTests {
  private W65C02 cpu;

  @BeforeEach
  void beforeEach() {
    cpu = new W65C02();
    TestUtils.load(cpu, 0x00, "01 01 02 03 05 08 0D 15 22 37 59 90 E9");
  }

  @ParameterizedTest
  @MethodSource("tests")
  void test(Parameters params) {
    TestUtils.executeTest(params, cpu);
  }

  static Stream<Parameters> tests() {
    return Stream.of(
        params("LDA", "A5 08", 3,
          Assertions.assertA(0x22)),

        params("LDA negative", "A5 0C", 3,
          Assertions::assertNegative,
          Assertions.assertA(0xE9)),

        params("AND", "A9 AA 25 0A", 5,
          Assertions::assertNotNegative,
          Assertions::assertNotZero,
          Assertions.assertA(0x08)),

        params("AND zero", "A9 AA 25 01", 5,
          Assertions::assertNotNegative,
          Assertions::assertZero,
          Assertions.assertA(0)),

        params("ORA", "A9 AA 05 09", 5,
          Assertions::assertNegative,
          Assertions::assertNotZero,
          Assertions.assertA(0xBF)),

        params("EOR", "A9 FF 45 09", 5,
            Assertions::assertNegative,
            Assertions::assertNotZero,
            Assertions.assertA(0xC8)),

        params("LDX", "A6 0A", 3,
            Assertions::assertNotNegative,
            Assertions::assertNotZero,
            Assertions.assertX(0x59)),

        params("LDY", "A4 0B", 3,
            Assertions::assertNegative,
            Assertions::assertNotZero,
            Assertions.assertY(0x90)),

        params("CMP", "A9 90 C5 0B", 5,
            Assertions::assertNotNegative,
            Assertions::assertZero,
            Assertions::assertCarry,
            Assertions.assertA(0x90)),

        params("CPY", "A0 90 C4 0B", 5,
            Assertions::assertNotNegative,
            Assertions::assertZero,
            Assertions::assertCarry,
            Assertions.assertY(0x90)),

        params("CPX", "A2 90 E4 0B", 5,
            Assertions::assertNotNegative,
            Assertions::assertZero,
            Assertions::assertCarry,
            Assertions.assertX(0x90)),

        params("BIT", "A9 0F 24 0C", 5,
            Assertions::assertNotZero,
            Assertions::assertNegative,
            Assertions::assertOverflow,
            Assertions.assertA(0x0F)),

        params("ADC", "A9 01 18 65 06", 7,
            Assertions::assertNotZero,
            Assertions::assertNotNegative,
            Assertions.assertA(0x0E)),

        params("STA", "A9 23 85 80", 5,
            Assertions.assertMemory(0x0080, 0x23)),

        params("STX", "A2 23 86 80", 5,
            Assertions.assertMemory(0x0080, 0x23)),

        params("STY", "A0 23 84 80", 5,
            Assertions.assertMemory(0x0080, 0x23)),

        params("STZ", "64 08", 3,
            Assertions.assertMemory(0x0008, 0)),

        params("INC", "E6 00", 5,
            Assertions::assertNotZero,
            Assertions::assertNotNegative,
            Assertions.assertMemory(0x0000, 0x02)),

        params("DEC", "C6 00", 5,
            Assertions::assertZero,
            Assertions::assertNotNegative,
            Assertions.assertMemory(0x0000, 0)),

        params("ASL with high bit clear", "06 0A", 5,
            Assertions::assertNotCarry,
            Assertions::assertNotZero,
            Assertions::assertNegative,
            Assertions.assertMemory(0x000A, 0xB2)),

        params("ASL with high bit set", "06 0B", 5,
            Assertions::assertCarry,
            Assertions::assertNotZero,
            Assertions::assertNotNegative,
            Assertions.assertMemory(0x000B, 0x20)),

        params("ROL with high bit and carry set", "38 26 0B", 7,
            Assertions::assertCarry,
            Assertions::assertNotZero,
            Assertions::assertNotNegative,
            Assertions.assertMemory(0x000B, 0x21)),

        params("ROL with high bit clear and carry set", "38 26 00", 7,
            Assertions::assertNotCarry,
            Assertions::assertNotZero,
            Assertions::assertNotNegative,
            Assertions.assertMemory(0x0000, 0x03)),

        params("ROL with high bit set and carry clear", "18 26 0B", 7,
            Assertions::assertCarry,
            Assertions::assertNotZero,
            Assertions::assertNotNegative,
            Assertions.assertMemory(0x000B, 0x20)),

        params("ROL with high bit and carry clear", "18 26 00", 7,
            Assertions::assertNotCarry,
            Assertions::assertNotZero,
            Assertions::assertNotNegative,
            Assertions.assertMemory(0x0000, 0x02)),

        params("LSR", "38 46 00", 7,
            Assertions::assertCarry,
            Assertions::assertZero,
            Assertions::assertNotNegative,
            Assertions.assertMemory(0x1000, 0)),

        params("ROR", "38 66 02", 7,
            Assertions::assertNotCarry,
            Assertions::assertNotZero,
            Assertions::assertNegative,
            Assertions.assertMemory(0x0002, 0x81)),

        params("TSB", "A9 AA 04 09", 7,
            Assertions::assertNotZero,
            Assertions.assertMemory(0x0009, 0xBF)),

        params("TSB no matching bits", "A9 88 04 09", 7,
            Assertions::assertZero,
            Assertions.assertMemory(0x0009, 0xBF)),

        params("TRB", "A9 AA 14 09", 7,
            Assertions::assertNotZero,
            Assertions.assertMemory(0x0009, 0x15)),

        params("TRB no matching bits", "A9 88 14 09", 7,
            Assertions::assertZero,
            Assertions.assertMemory(0x0009, 0x37)),

        params("RMB6", "67 0C", 5,
            Assertions.assertMemory(0x000C, 0xA9)),

        params("SMB4", "C7 0C", 5,
            Assertions.assertMemory(0x000C, 0xF9)),

        params("SBC", "A9 05 38 E5 03", 7,
            Assertions::assertNotZero,
            Assertions::assertNotNegative,
            Assertions.assertA(0x02))
    );
  }
}
