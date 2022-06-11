/* Copyright (C) 2021, 2022 Joseph Vigneau */

package joev.ya6s;

import static joev.ya6s.Parameters.params;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class RelativeTests {
  private Backplane backplane;
  private W65C02S cpu;
  private SRAM ram;

  @BeforeEach
  void beforeEach() {
    backplane = new Backplane();
    cpu = new W65C02S(backplane);
    ram = new SRAM(backplane);

    TestUtils.load(backplane, cpu, 0x0260, "A9 42 DB");
  }

  @ParameterizedTest
  @MethodSource("tests")
  void test(Parameters params) {
    TestUtils.executeTest(params, backplane, cpu);
  }

  static Stream<Parameters> tests() {
    return Stream.of(
      params("BRA", """
        80 5E ; BRA #$5E  ($260)
        A9 23 ; LDA #$23
        """, 5,
        Assertions.assertA(0x42)),

      params("BPL", """
        A9 01 ; LDA #$01
        10 5C ; BPL #$5C ($260)
        A9 23 ; LDA #$23
        """, 7,
        Assertions.assertA(0x42)),

      params("BMI", """
        A9 81 ; LDA #$81
        30 5C ; BMI #$5C ($260)
        A9 23 ; LDA #$23
        """, 7,
        Assertions.assertA(0x42)),

      params("BCC", """
        18    ; CLC
        90 5D ; BCC #$5D ($260)
        A9 23 ; LDA #$23
        """, 7,
        Assertions.assertA(0x42)),

      params("BCS", """
        38    ; SEC
        B0 5D ; BCS #$5D ($260)
        A9 23 ; LDA #$23
        """, 7,
        Assertions.assertA(0x42)),

      params("BNE", """
        A9 01 ; LDA #$01
        D0 5C ; BNE #$5C ($260)
        A9 23 ; LDA #$23
        """, 7,
        Assertions.assertA(0x42)),

      params("BEQ", """
        A9 00 ; LDA #$00
        F0 5C ; BEQ #$5C ($260)
        A9 23 ; LDA #$23
        """, 7,
        Assertions.assertA(0x42)),

      params("BVC", """
        A9 00 ; LDA #$00
        85 40 ; STA $40
        24 40 ; BIT $40
        50 58 ; BVC #$58 ($260)
        A9 23 ; LDA #$23
        """, 13,
        Assertions.assertA(0x42)),

      params("BVS", """
        A9 40 ; LDA #$40
        85 40 ; STA $40
        24 40 ; BIT $40
        70 58 ; BVS #$58 ($260)
        A9 23 ; LDA #$23
        """, 13,
        Assertions.assertA(0x42)),

      params("BEQ false", """
        A9 01 ; LDA #$01,
        F0 5C ; BEQ #$5C ($260)
        A9 23 ; LDA #$23
        """, 6,
        Assertions.assertA(0x23)),

      params("BBR5", """
        A9 DF    ; LDA #$DF
        85 33    ; STA $33
        5F 33 59 ; BBR5 $33,$#59 ($260)
        A9 23    ; LDA #$23
        """, 13,
        Assertions.assertA(0x42)),

      params("BBR5 false", """
        A9 20    ; LDA #$20
        85 33    ; STA $33
        5F 33 59 ; BBR5 $33,$#59 ($260)
        A9 23    ; LDA #$23
        """, 12,
        Assertions.assertA(0x23)),

      params("BBS4", """
        A9 10    ; LDA #$10
        85 33    ; STA $33
        CF 33 59 ; BBS4 $33,$#59 ($260)
        A9 23    ; LDA #$23
        """, 13,
        Assertions.assertA(0x42)),

      params("BBS4 false", """
        A9 EF    ; LDA #$EF
        85 33    ; STA $33
        CF 33 59 ; BBS4 $33,$#59 ($260)
        A9 23    ; LDA #$23
        """, 12,
        Assertions.assertA(0x23))
    );
  }
}
