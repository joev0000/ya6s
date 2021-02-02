package joev.ya6s;

import java.util.function.Consumer;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Miscellaneous test utilities.
 */
public class TestUtils {

  /**
   * Load data into memory at a given address.
   *
   * The data is formatted as a String with hex values separated by
   * whitespace.  For example
   *
   *     LDA $#23
   *
   * can be encoded as "A9 23"
   *
   * @param cpu the CPU whose memory is being loaded into.
   * @param address the address of where the data is to be loaded.
   * @param data the data to load.
   */
  public static void load(W65C02 cpu, int address, String data) {
    String[] hex = data.split("[ \t\n\r]");
    for(int i = 0; i < hex.length; i++) {
      cpu.write((short)address++, Integer.decode(("0x" + hex[i])).byteValue());
    }
  }

  /**
   * Run a program by setting the startAddress to the RESET vector, then
   * execute a reset.
   *
   * @param cpu the CPU to run the program on.
   * @param startAddress the starting address of the program.
   * @param maxCycles the maximum number of cycles to execute, exceeding this
   *   causes a CyclesExceededException to be thrown.
   */
  public static int run(W65C02 cpu, int startAddress, int maxCycles) {
    int cycles = 0;
    cpu.write((short)0xFFFC, (byte)(startAddress & 0xFF));
    cpu.write((short)0xFFFD, (byte)(startAddress >> 8));
    cpu.reset();

    while(!cpu.stopped()) {
      if(cycles > maxCycles) {
        throw new CyclesExceededException(cycles);
      }
      cycles++;
      cpu.tick();
    }
    return cycles-1;
  }

  /**
   * Execute a test on the cpu with the given test parameters.
   *
   *
   * @param params the parameters of the test to run.
   * @param cpu the CPU to run the test on.
   */
  public static void executeTest(Parameters params, W65C02 cpu) {
    String program = params.program() + " DB";
    int cycles = params.cycles() + 3;
    Consumer<W65C02>[] asserts = params.asserts();

    load(cpu, 0x200, program);
    assertEquals(cycles, run(cpu, 0x200, cycles));
    for(int i = 0; i < asserts.length; i++) {
      asserts[i].accept(cpu);
    }
  }
}
