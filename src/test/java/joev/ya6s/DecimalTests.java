package joev.ya6s;

import static joev.ya6s.Parameters.params;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class DecimalTests {
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
        params("ADC $57 + $46", "F8 A9 57 69 46", 6,
          Assertions::assertNotZero,
          Assertions::assertCarry,
          Assertions::assertNotNegative,
          Assertions::assertNotOverflow,
          Assertions.assertA(0x03)),
 
        params("ADC basic", "F8 A9 29 69 15", 6,
          Assertions::assertNotZero,
          Assertions::assertNotNegative,
          Assertions::assertNotCarry,
          Assertions::assertNotOverflow,
          Assertions.assertA(0x44))
    );
  }
}

