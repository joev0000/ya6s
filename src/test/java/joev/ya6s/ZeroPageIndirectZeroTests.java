package joev.ya6s;

import static joev.ya6s.Parameters.params;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class ZeroPageIndirectZeroTests {
  private Backplane backplane;
  private W65C02S cpu;
  private SRAM ram;

  @BeforeEach
  void beforeEach() {
    backplane = new Backplane();
    cpu = new W65C02S(backplane);
    ram = new SRAM(backplane);

    TestUtils.load(backplane, cpu, 0x0040, "20 12");
    TestUtils.load(backplane, cpu, 0x1220, "42");
  }

  @ParameterizedTest
  @MethodSource("tests")
  void test(Parameters params) {
    TestUtils.executeTest(params, backplane, cpu);
  }

  static Stream<Parameters> tests() {
    return Stream.of(
      params("LDA", """
        B2 40 ; LDA ($40)
        """, 5,
        Assertions.assertA(0x42))
      );
  }
}

