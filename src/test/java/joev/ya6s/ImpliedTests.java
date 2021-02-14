package joev.ya6s;

import static joev.ya6s.Parameters.params;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class ImpliedTests {
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
      params("INC", """
        A9 7F ; LDA #$7F
        1A    ; INC
      """, 4,
      Assertions::assertNegative,
      Assertions::assertNotZero,
      Assertions.assertA(0x80)),

      params("INC", """
        A9 01 ; LDA #$01
        3A    ; DEC
      """, 4,
      Assertions::assertNotNegative,
      Assertions::assertZero,
      Assertions.assertA(0x00)),

      params("ASL", """
        A9 EA ; LDA #$EA
        0A    ; ASL
        """, 4,
        Assertions::assertCarry,
        Assertions::assertNegative,
        Assertions::assertNotZero,
        Assertions.assertA(0xD4)),

      params("ROL", """
        38    ; SEC
        A9 EA ; LDA #$EA
        2A    ; ROL
        """, 6,
        Assertions::assertCarry,
        Assertions::assertNegative,
        Assertions::assertNotZero,
        Assertions.assertA(0xD5)),

      params("LSR", """
        38    ; SEC
        A9 55 ; LDA #$55
        4A    ; LSR
        """, 6,
        Assertions::assertCarry,
        Assertions::assertNotNegative,
        Assertions::assertNotZero,
        Assertions.assertA(0x2A)),

      params("ROR", """
        38    ; SEC
        A9 55 ; LDA #$55
        6A    ; ROR
      """, 6,
      Assertions::assertCarry,
      Assertions::assertNegative,
      Assertions::assertNotZero,
      Assertions.assertA(0xAA))
    );
  }
}
