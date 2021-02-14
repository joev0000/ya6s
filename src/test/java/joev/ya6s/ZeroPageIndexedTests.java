package joev.ya6s;

import static joev.ya6s.Parameters.params;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class ZeroPageIndexedTests {
  private Backplane backplane;
  private W65C02 cpu;
  private SRAM ram;

  @BeforeEach
  void beforeEach() {
    backplane = new Backplane();
    cpu = new W65C02(backplane);
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
        64 80    ; STZ $80
        85 81    ; STA $81
        A9 23    ; LDA #$23
        A0 24    ; LDY #$24
        91 80    ; STA ($80),Y
        A9 42    ; LDA #$42
        AD 24 10 ; LDA $1024
        """, 23,
        Assertions.assertA(0x23)),

      params("LDA", """
        A9 10    ; LDA #$10
        64 80    ; STZ $80
        85 81    ; STA $81
        A9 23    ; LDA #$23
        8D 24 10 ; STA $1024
        A9 42    ; LDA #$42
        A0 24    ; LDY #$24
        B1 80    ; LDA ($80),Y
        """, 23,
        Assertions.assertA(0x23))
      );
  }
}
