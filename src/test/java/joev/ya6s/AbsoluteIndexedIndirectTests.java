package joev.ya6s;

import static joev.ya6s.Parameters.params;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class AbsoluteIndexedIndirectTests {
  private Backplane backplane;
  private W65C02 cpu;
  private SRAM ram;

  @BeforeEach
  void beforeEach() {
    backplane = new Backplane();
    cpu = new W65C02(backplane);
    ram = new SRAM(backplane);

    TestUtils.load(backplane, cpu, 0x2124, """
      A9 42 ; LDA #$42
      DB    ; STP
    """);

    TestUtils.load(backplane, cpu, 0x3020, "24 21");
  }

  @ParameterizedTest
  @MethodSource("tests")
  void test(Parameters params) {
    TestUtils.executeTest(params, backplane, cpu);
  }

  static Stream<Parameters> tests() {
    return Stream.of(
      params("JMP", """
        A2 20    ; LDX #$20
        7C 00 30 ; JMP ($3000,X)
        """, 10,
        Assertions.assertA(0x42))
    );
  }
}
