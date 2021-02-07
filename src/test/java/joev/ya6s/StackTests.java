package joev.ya6s;

import static joev.ya6s.Parameters.params;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class StackTests {
  private W65C02 cpu;

  @BeforeEach
  void beforeEach() {
    cpu = new W65C02();
    TestUtils.load(cpu, 0xF000, "A9 23 DB");
    TestUtils.load(cpu, 0xFFFE, "00 F0");
  }

  @ParameterizedTest
  @MethodSource("tests")
  void test(Parameters params) {
    TestUtils.executeTest(params, cpu);
  }

  static Stream<Parameters> tests() {
    return Stream.of(
      params("BRK", "00", 9,
        Assertions.assertA(0x23))
    );
  }
}

