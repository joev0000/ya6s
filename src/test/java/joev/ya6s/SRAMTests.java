package joev.ya6s;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import joev.ya6s.signals.Bus;
import joev.ya6s.signals.Signal;

/**
 * Tests for the SRAM device.
 */
public class SRAMTests {
  private SRAM sram;
  private Backplane backplane;
  private Signal clock;
  private Signal rwb;
  private Bus addressBus;
  private Bus dataBus;

  /**
   * Create a new Backplane and get the Signals for use in tests.
   */
  @BeforeEach
  void beforeEach() {
    backplane = new Backplane();
    clock = backplane.clock();
    addressBus = backplane.address();
    dataBus = backplane.data();
    rwb = backplane.rwb();
  }


  /**
   * Write a byte to the bus.
   *
   * @param address the address to write
   * @param value the value to write to the address
   */
  private void write(int address, byte value) {
    addressBus.value((short)address);
    dataBus.value(value);
    rwb.value(false);
    clock.value(false);
    clock.value(true);
  }

  /**
   * Read a byte from the bus.
   *
   * @param address the address to read
   * @return the value at the address
   */
  private byte read(int address) {
    addressBus.value((short)address);
    rwb.value(true);
    clock.value(false);
    clock.value(true);
    return (byte)dataBus.value();
  }

  /**
   * Check to see if a write to an address can be read.
   *
   * @param address the address to write and read
   * @param expectSuccess true if we expect to be able to read the value
   *                      that was written
   */
  private void check(int address, boolean expectSuccess) {
    byte value = (byte)((address & 0xFF) ^ 0xAA);
    if(value == (byte)0xEE) {
      value++;
    }
    write(address, value);
    dataBus.value((byte)0xEE);
    assertEquals(expectSuccess ? value : (byte)0xEE, read(address));
  }

  /**
   * Test a 32k SRAM configuration, starting at address 0x0000.
   */
  @Test
  void test32k() {
    sram = new SRAM(backplane, (short)0, 0x8000);

    check(0x0000, true);
    check(0x7FFF, true);
    check(0x8000, false);
  }

  /**
   * Test a 48k SRAM configuration, starting at address 0x0000.
   */
  @Test
  void test48k() {
    sram = new SRAM(backplane, (short)0, 0xC000);

    check(0x0000, true);
    check(0xBFFF, true);
    check(0xC000, false);
  }

  /**
   * Test an SRAM configuration that does not start at 0x000, and
   * has a size that does not align to a page.
   */
  @Test
  void nonNonStandardBaseAndSize() {
    int base = 0x8123;
    int size = 0x1234;
    sram = new SRAM(backplane, (short)base, size);
    check(base - 1,        false);
    check(base,            true);
    check(base + size - 1, true);
    check(base + size,     false);
  }
}
