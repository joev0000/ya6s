package joev.ya6s;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.Consumer;

import joev.ya6s.signals.Bus;
import joev.ya6s.signals.Signal;

/**
 * Utilits for running the unit tests.
 */
public class TestUtils {
  /**
   * Run the system, throwing an exception if the program runs too long.
   *
   * @param backplane the backplane of the system.
   * @param cpu the cpu of the system.
   * @param maxCycles the maxiumum number of cycles to run.
   * @throws CyclesExceededException if the program does not stop before maxCycles
   */
  public static int run(Backplane backplane, W65C02 cpu, int maxCycles) {
    int cycles = 0;
    Signal clock = backplane.clock();
    cpu.resb().value(true);
    while(!cpu.stopped()) {
      if(cycles == maxCycles)
        throw new CyclesExceededException(maxCycles);
      clock.value(true);
      clock.value(false);
      cycles++;
    }
    return cycles;
  }

  /**
   * Load data into the system.
   *
   * The hex parameter is one or more lines of hex values separated by whitespace.
   * Anything after a semicolon on the line is ignored.  For example:
   *
   *   A9 23 ; LDA #$23
   *   A2 42 ; LDX #$42
   *
   * The CPU has its rdy line set to fales while the bytes are clocked into memory.
   *
   * @param backplane the backplane of the system.
   * @param cpu the cpu of the system.
   * @param location the location within memory where the bytes will be written.
   * @param hex the hex bytes to write into the system.
   */
  public static void load(Backplane backplane, W65C02 cpu, int location, String hex) {
    Bus address = backplane.address();
    Bus data = backplane.data();
    Signal rwb = backplane.rwb();
    Signal clock = backplane.clock();
    Signal rdy = cpu.rdy();

    rdy.value(false);
    String[] lines = hex.split("\n");
    for(String line: lines) {
      String trimmed = line.split(";")[0].trim();
      if(!trimmed.isBlank()) {
        String[] bytes = trimmed.split("[ \t]+");
        for(String h: bytes) {
          clock.value(false);
          address.value(location++);
          data.value(Integer.parseInt(h, 16));
          rwb.value(false);
          clock.value(true);
        }
      }
    }
    rdy.value(true);
  }

  public static void executeTest(Parameters params) {
    Backplane backplane = new Backplane();
    W65C02 cpu = new W65C02(backplane);
    SRAM ram = new SRAM(backplane);

    executeTest(params, backplane, cpu);
  }

  public static void executeTest(Parameters params, Backplane backplane, W65C02 cpu) {
    TestUtils.load(backplane, cpu, 0x200, params.program() + "\nDB");
    TestUtils.load(backplane, cpu, 0xFFFC, "00 02");

    int cycles = params.cycles() + 7 + 3;  // + reset + STP
    assertEquals(cycles, TestUtils.run(backplane, cpu, cycles));
    for(Consumer<W65C02> assertion: params.asserts()) {
      assertion.accept(cpu);
    }
  }
}

