package joev.ya6s;

import static joev.ya6s.Parameters.params;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class ImmediateTests {
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
        params("LDA", "A9 23", 2,
          Assertions::assertNotNegative,
          Assertions::assertNotZero,
          Assertions.assertA(0x23)),

        params("LDA zero", "A9 00", 2,
          Assertions::assertNotNegative,
          Assertions::assertZero,
          Assertions.assertA(0)),

        params("LDA negative", "A9 80", 2,
          Assertions::assertNegative,
          Assertions::assertNotZero,
          Assertions.assertA(0x80)),

        params("AND", "A9 AA 29 8F", 4,
          Assertions::assertNegative,
          Assertions::assertNotZero,
          Assertions.assertA(0x8A)),

        params("AND zero", "A9 AA 29 55", 4,
            Assertions::assertNotNegative,
            Assertions::assertZero,
            Assertions.assertA(0)),

        params("ORA", "A9 AA 09 FF", 4,
            Assertions::assertNegative,
            Assertions::assertNotZero,
            Assertions.assertA(0xFF)),

        params("EOR", "A9 AA 49 FF", 4,
            Assertions::assertNotNegative,
            Assertions::assertNotZero,
            Assertions.assertA(0x55)),

        params("LDX", "A2 23", 2,
            Assertions::assertNotNegative,
            Assertions::assertNotZero,
            Assertions.assertX(0x23)),

        params("LDY", "A0 23", 2,
            Assertions::assertNotNegative,
            Assertions::assertNotZero,
            Assertions.assertY(0x23)),

        params("CMP equal", "A9 20 C9 20", 4,
            Assertions::assertNotNegative,
            Assertions::assertZero,
            Assertions::assertCarry,
            Assertions.assertA(0x20)),

        params("CMP greater than", "A9 21 C9 20", 4,
            Assertions::assertNotNegative,
            Assertions::assertNotZero,
            Assertions::assertCarry,
            Assertions.assertA(0x21)),

        params("CMP less than", "A9 20 C9 21", 4,
            Assertions::assertNegative,
            Assertions::assertNotZero,
            Assertions::assertNotCarry,
            Assertions.assertA(0x20)),

        params("CPY", "A0 20 C0 20", 4,
            Assertions::assertNotNegative,
            Assertions::assertZero,
            Assertions::assertCarry,
            Assertions.assertY(0x20)),

        params("CPY greater than", "A0 21 C0 20", 4,
            Assertions::assertNotNegative,
            Assertions::assertNotZero,
            Assertions::assertCarry,
            Assertions.assertY(0x21)),

        params("CPX", "A2 20 E0 20", 4,
            Assertions::assertNotNegative,
            Assertions::assertZero,
            Assertions::assertCarry,
            Assertions.assertX(0x20)),

        params("CPX greater than", "A2 21 E0 20", 4,
            Assertions::assertNotNegative,
            Assertions::assertNotZero,
            Assertions::assertCarry,
            Assertions.assertX(0x21)),

        params("BIT", "A9 0F 89 C0", 4,
            Assertions::assertZero,
            Assertions::assertNegative,
            Assertions::assertOverflow,
            Assertions.assertA(0x0F)),

        params("BIT set overflow", "89 40", 2,
            Assertions::assertOverflow),

        params("ADC", "D8 18 A9 01 69 02", 8,
            Assertions::assertNotZero,
            Assertions::assertNotNegative,
            Assertions::assertNotCarry,
            Assertions::assertNotOverflow,
            Assertions.assertA(0x03)),

        params("ADC with carry", "D8 38 A9 01 69 02", 8,
            Assertions::assertNotZero,
            Assertions::assertNotNegative,
            Assertions::assertNotCarry,
            Assertions::assertNotOverflow,
            Assertions.assertA(0x04)),

        params("ADC zero", "D8 18 A9 01 69 FF", 8,
            Assertions::assertZero,
            Assertions::assertNotNegative,
            Assertions::assertCarry,
            Assertions::assertNotOverflow,
            Assertions.assertA(0)),

        params("ADC overflow", "D8 18 A9 7F 69 02", 8,
            Assertions::assertNotZero,
            Assertions::assertNegative,
            Assertions::assertNotCarry,
            Assertions::assertOverflow,
            Assertions.assertA(0x81)),

        params("SBC", "D8 38 A9 03 E9 02", 8,
            Assertions::assertNotZero,
            Assertions::assertNotNegative,
            Assertions::assertNotCarry,
            Assertions::assertNotOverflow,
            Assertions.assertA(0x01)),

        params("SBC without borrow", "D8 18 A9 03 E9 02", 8,
            Assertions::assertZero,
            Assertions::assertNotNegative,
            Assertions::assertNotCarry,
            Assertions::assertNotOverflow,
            Assertions.assertA(0))
    );
  }
}
