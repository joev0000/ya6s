package joev.ya6s;

import static joev.ya6s.Parameters.params;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class JumpTests {
  private W65C02 cpu;

  @BeforeEach
  void beforeEach() {
    cpu = new W65C02();
    TestUtils.load(cpu, 0x1000, "A9 23 DB");
    TestUtils.load(cpu, 0x2000, "00 10");
  }

  @ParameterizedTest
  @MethodSource("tests")
  void test(Parameters params) {
    TestUtils.executeTest(params, cpu);
  }

  static Stream<Parameters> tests() {
    return Stream.of(
      params("JMP", "4C 00 10", 5,
        Assertions.assertA(0x23)),

      params("JMP indirect", "6C 00 20", 7,
        Assertions.assertA(0x23)),

      params("JMP indirect indexed", "A2 10 7C F0 1F", 10,
        Assertions.assertA(0x023))
    );
  }
}
