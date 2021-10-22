package joev.ya6s;

import static joev.ya6s.Parameters.params;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class AbsoluteIndirectTests {
  private Backplane backplane;
  private W65C02S cpu;
  private SRAM ram;

  @BeforeEach
  void beforeEach() {
    backplane = new Backplane();
    cpu = new W65C02S(backplane);
    ram = new SRAM(backplane);

    TestUtils.load(backplane, cpu, 0x3000, """
      A9 42 ; LDA #$42
      DB    ; STP
    """);
  }

  @ParameterizedTest
  @MethodSource("tests")
  void test(Parameters params) {
    TestUtils.executeTest(params, backplane, cpu);
  }

  static Stream<Parameters> tests() {
    return Stream.of(
      params("JMP", """
        A9 30    ; LDA #$30
        9C 00 10 ; STZ $1000
        8D 01 10 ; STA $1001
        6C 00 10 ; JMP ($1000)
        A9 23    ; LDA #$23
        """, 18,
        Assertions.assertA(0x42))
     );
  }
}

