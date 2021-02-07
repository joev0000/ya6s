package joev.ya6s;

import static joev.ya6s.Parameters.params;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class RelativeBitTests {
  private W65C02 cpu;

  @BeforeEach
  void beforeEach() {
    cpu = new W65C02();
    TestUtils.load(cpu, 0x0000, "01 01 02 03 05 08 0D 15 22 37 59 90 E9");
    TestUtils.load(cpu, 0x0220, "A9 42 DB");
  }

  @ParameterizedTest
  @MethodSource("tests")
  void test(Parameters params) {
    TestUtils.executeTest(params, cpu);
  }

  static Stream<Parameters> tests() {
    return Stream.of(
      params("BBR0 bit 0 set", "0F 00 1D A9 23", 7,
        Assertions.assertA(0x23)),

      params("BBR0 bit 0 clear", "0F 02 1D A9 23", 8,
        Assertions.assertA(0x42)),

      params("BBR5 bit 5 set", "5F 08 1D A9 23", 7,
        Assertions.assertA(0x23)),

      params("BBR5 bit 5 clear", "5F 00 1D A9 23", 8,
        Assertions.assertA(0x42)),

      params("BBR7 bit 7 set", "7F 0B 1D A9 23", 7,
        Assertions.assertA(0x23)),

      params("BBR7 bit 7 clear", "7F 02 1D A9 23", 8,
        Assertions.assertA(0x42)),

      params("BBS0 bit 0 set", "8F 00 1D A9 23", 8,
        Assertions.assertA(0x42)),

      params("BBS0 bit 0 clear", "8F 02 1D A9 23", 7,
        Assertions.assertA(0x23)),

      params("BBS5 bit 5 set", "DF 08 1D A9 23", 8,
        Assertions.assertA(0x42)),

      params("BBS5 bit 5 clear", "DF 00 1D A9 23", 7,
        Assertions.assertA(0x23)),

      params("BBS7 bit 7 set", "FF 0B 1D A9 23", 8,
        Assertions.assertA(0x42)),

      params("BBS7 bit 7 clear", "FF 02 1D A9 23", 7,
        Assertions.assertA(0x23))
      );
  }
}

