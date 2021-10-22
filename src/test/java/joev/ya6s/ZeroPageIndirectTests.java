package joev.ya6s;

import static joev.ya6s.Parameters.params;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class ZeroPageIndirectTests {
  private Backplane backplane;
  private W65C02S cpu;
  private SRAM ram;

  @BeforeEach
  void beforeEach() {
    backplane = new Backplane();
    cpu = new W65C02S(backplane);
    ram = new SRAM(backplane);
  }

  @ParameterizedTest
  @MethodSource("tests")
  void test(Parameters params) {
    TestUtils.executeTest(params, backplane, cpu);
  }

  static Stream<Parameters> tests() {
    return Stream.of(
      params("STA", """
        A9 10    ; LDA #$10
        64 84    ; STZ $84
        85 85    ; STA $85
        A9 23    ; LDA #$23
        A2 04    ; LDX #$04
        81 80    ; STA ($80,X)
        A9 42    ; LDA #$42
        AD 00 10 ; LDA $1000
        """, 24,
        Assertions.assertA(0x23)),

      params("LDA", """
        A9 23    ; LDA #$23
        8D 30 20 ; STA $2030
        A9 30    ; LDA #$30
        85 62    ; STA $62
        A9 20    ; LDA #$20
        85 63    ; STA $63
        A2 02    ; LDX #$02
        A1 60    ; LDA ($60,X)
        """, 24,
        Assertions.assertA(0x23))
    );
  }
}
