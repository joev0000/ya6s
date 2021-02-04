package joev.ya6s;

import static joev.ya6s.Parameters.params;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class AbsoluteYTests {
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
      params("LDA", "A0 0C B9 00 10", 6,
        Assertions::assertNotZero,
        Assertions::assertNegative,
        Assertions.assertA(0xE9))
    );
  }
}


