/* Copyright (C) 2021, 2022 Joseph Vigneau */

package joev.ya6s;

import static joev.ya6s.Parameters.params;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class BreakTests {
  private Backplane backplane;
  private W65C02S cpu;
  private SRAM ram;

  @BeforeEach
  void beforeEach() {
    backplane = new Backplane();
    cpu = new W65C02S(backplane);
    ram = new SRAM(backplane);

    TestUtils.load(backplane, cpu, 0x3000, """
      A2 42 ; LDX #$42
      18    ; CLC
      40    ; RTI
    """);

    TestUtils.load(backplane, cpu, 0xFFFE, "00 30");
  }

  @ParameterizedTest
  @MethodSource("tests")
  void test(Parameters params) {
    TestUtils.executeTest(params, backplane, cpu);
  }

  static Stream<Parameters> tests() {
    return Stream.of(
      params("BRK", """
        38    ; SEC
        00 99 ; BRK 99
        A9 23 ; LDA #$23
        """, 21,
        Assertions::assertCarry,
        Assertions.assertA(0x23),
        Assertions.assertX(0x42))

    );
  }
}
