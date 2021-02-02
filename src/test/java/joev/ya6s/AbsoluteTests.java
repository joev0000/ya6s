package joev.ya6s;

import static joev.ya6s.Parameters.params;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class AbsoluteTests {
  private W65C02 cpu;

  @BeforeEach
  void beforeEach() {
    cpu = new W65C02();
    TestUtils.load(cpu, 0x1000, "01 01 02 03 05 08 0D 15 22 37 59 90 E9");
  }

  @ParameterizedTest
  @MethodSource("tests")
  void test(Parameters params) {
    TestUtils.executeTest(params, cpu);
  }

  static Stream<Parameters> tests() {
    return Stream.of(
      params("LDA", "AD 08 10", 4,
        Assertions.assertA(0x22)),

      params("LDA negative", "AD 0C 10", 4,
        Assertions::assertNegative,
        Assertions.assertA(0xE9)),

      params("AND", "A9 AA 2D 0A 10", 6,
        Assertions::assertNotNegative,
        Assertions::assertNotZero,
        Assertions.assertA(0x08)),

      params("AND zero", "A9 AA 2D 01 10", 6,
        Assertions::assertNotNegative,
        Assertions::assertZero,
        Assertions.assertA(0)),

      params("ORA", "A9 AA 0D 09 10", 6,
        Assertions::assertNegative,
        Assertions::assertNotZero,
        Assertions.assertA(0xBF)),

      params("EOR", "A9 FF 4D 09 10", 6,
          Assertions::assertNegative,
          Assertions::assertNotZero,
          Assertions.assertA(0xC8)),

      params("LDX", "AE 0A 10", 4,
          Assertions::assertNotNegative,
          Assertions::assertNotZero,
          Assertions.assertX(0x59)),

      params("LDY", "AC 0B 10", 4,
          Assertions::assertNegative,
          Assertions::assertNotZero,
          Assertions.assertY(0x90)),

      params("CMP", "A9 90 CD 0B 10", 6,
          Assertions::assertNotNegative,
          Assertions::assertZero,
          Assertions::assertCarry,
          Assertions.assertA(0x90)),

      params("CPY", "A0 90 CC 0B 10", 6,
          Assertions::assertNotNegative,
          Assertions::assertZero,
          Assertions::assertCarry,
          Assertions.assertY(0x90)),

      params("CPX", "A2 90 EC 0B 10", 6,
          Assertions::assertNotNegative,
          Assertions::assertZero,
          Assertions::assertCarry,
          Assertions.assertX(0x90)),

      params("BIT", "A9 0F 2C 0C 10", 6,
          Assertions::assertNotZero,
          Assertions::assertNegative,
          Assertions::assertOverflow,
          Assertions.assertA(0x0F)),

      params("ADC", "A9 01 18 6D 06 10", 8,
          Assertions::assertNotZero,
          Assertions::assertNotNegative,
          Assertions.assertA(0x0E)),

      params("STA", "A9 23 8D 80 10", 6,
          Assertions.assertMemory(0x1080, 0x23)),

      params("STX", "A2 23 8E 80 10", 6,
          Assertions.assertMemory(0x1080, 0x23)),

      params("STY", "A0 23 8C 80 10", 6,
          Assertions.assertMemory(0x1080, 0x23)),

      params("STZ", "9C 08 10", 4,
          Assertions.assertMemory(0x1008, 0)),

      params("INC", "EE 00 10", 6,
          Assertions::assertNotZero,
          Assertions::assertNotNegative,
          Assertions.assertMemory(0x1000, 0x02)),

      params("DEC", "CE 00 10", 6,
          Assertions::assertZero,
          Assertions::assertNotNegative,
          Assertions.assertMemory(0x1000, 0)),

      params("ASL with high bit clear", "0E 0A 10", 6,
          Assertions::assertNotCarry,
          Assertions::assertNotZero,
          Assertions::assertNegative,
          Assertions.assertMemory(0x100A, 0xB2)),

      params("ASL with high bit set", "0E 0B 10", 6,
          Assertions::assertCarry,
          Assertions::assertNotZero,
          Assertions::assertNotNegative,
          Assertions.assertMemory(0x100B, 0x20)),

      params("ROL with high bit and carry set", "38 2E 0B 10", 8,
          Assertions::assertCarry,
          Assertions::assertNotZero,
          Assertions::assertNotNegative,
          Assertions.assertMemory(0x100B, 0x21)),

      params("ROL with high bit clear and carry set", "38 2E 00 10", 8,
          Assertions::assertNotCarry,
          Assertions::assertNotZero,
          Assertions::assertNotNegative,
          Assertions.assertMemory(0x1000, 0x03)),

      params("ROL with high bit set and carry clear", "18 2E 0B 10", 8,
          Assertions::assertCarry,
          Assertions::assertNotZero,
          Assertions::assertNotNegative,
          Assertions.assertMemory(0x100B, 0x20)),

      params("ROL with high bit and carry clear", "18 2E 00 10", 8,
          Assertions::assertNotCarry,
          Assertions::assertNotZero,
          Assertions::assertNotNegative,
          Assertions.assertMemory(0x1000, 0x02)),

      params("LSR", "38 4E 00 10", 8,
          Assertions::assertCarry,
          Assertions::assertZero,
          Assertions::assertNotNegative,
          Assertions.assertMemory(0x1000, 0)),

      params("ROR", "38 6E 02 10", 8,
          Assertions::assertNotCarry,
          Assertions::assertNotZero,
          Assertions::assertNegative,
          Assertions.assertMemory(0x1002, 0x81)),

      params("TSB", "A9 AA 0C 09 10", 8,
          Assertions::assertNotZero,
          Assertions.assertMemory(0x1009, 0xBF)),

      params("TSB no matching bits", "A9 88 0C 09 10", 8,
          Assertions::assertZero,
          Assertions.assertMemory(0x1009, 0xBF)),

      params("TRB", "A9 AA 1C 09 10", 8,
          Assertions::assertNotZero,
          Assertions.assertMemory(0x1009, 0x15)),

      params("TRB no matching bits", "A9 88 1C 09 10", 8,
          Assertions::assertZero,
          Assertions.assertMemory(0x1009, 0x37)),

      params("SBC", "A9 05 38 ED 03 10", 8,
          Assertions::assertNotZero,
          Assertions::assertNotNegative,
          Assertions.assertA(0x02))
    );
  }
}

