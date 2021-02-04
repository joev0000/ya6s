package joev.ya6s;

import static joev.ya6s.Parameters.params;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class RelativeTests {
  private W65C02 cpu;

  @BeforeEach
  void beforeEach() {
    cpu = new W65C02();
    TestUtils.load(cpu, 0x0220, "A9 42 DB");
  }

  @ParameterizedTest
  @MethodSource("tests")
  void test(Parameters params) {
    TestUtils.executeTest(params, cpu);
  }

  static Stream<Parameters> tests() {
    return Stream.of(
        params("BRA", "80 1E", 5,
          Assertions.assertA(0x42)),

        params("BEQ taken", "A9 00 F0 1C A9 23", 7,
          Assertions.assertA(0x42)),

        params("BEQ not taken", "A9 FF F0 1C A9 23", 6,
          Assertions.assertA(0x23)),

        params("BNE taken", "A9 FF D0 1C A9 23", 7,
          Assertions.assertA(0x42)),

        params("BNE not taken", "A9 00 D0 1C A9 23", 6,
          Assertions.assertA(0x23)),

        params("BMI taken", "A9 FF 30 1C A9 23", 7,
          Assertions.assertA(0x42)),

        params("BMI not taken", "A9 00 30 1C A9 23", 6,
          Assertions.assertA(0x23)),

        params("BPL taken", "A9 00 10 1C A9 23", 7,
          Assertions.assertA(0x42)),

        params("BPL not taken", "A9 FF 10 1C A9 23", 6,
          Assertions.assertA(0x23)),

        params("BCC taken", "18 90 1D A9 23", 7,
            Assertions.assertA(0x42)),

        params("BCC not taken", "38 90 1D A9 23", 6,
          Assertions.assertA(0x23)),

        params("BCS taken", "38 B0 1D A9 23", 7,
            Assertions.assertA(0x42)),

        params("BCS not taken", "18 B0 1D A9 23", 6,
          Assertions.assertA(0x23)),

        params("BVS taken", "89 40 70 1C A9 23", 7,
          Assertions.assertA(0x42)),

        params("BVS not taken", "89 00 70 1C A9 23", 6,
          Assertions.assertA(0x23)),

        params("BVC taken", "89 00 50 1C A9 23", 7,
          Assertions.assertA(0x42)),

        params("BVC not taken", "89 40 50 1C A9 23", 6,
          Assertions.assertA(0x23))
    );
  }
}

