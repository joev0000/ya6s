/* Copyright (C) 2021, 2022 Joseph Vigneau */

package joev.ya6s;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import joev.ya6s.signals.Bus;
import joev.ya6s.signals.Signal;


/**
 * Tests for the ROM device.
 */
public class ROMTests {
  private Backplane backplane;
  private Clock clock;
  private Signal rwb;
  private Bus addressBus;
  private Bus dataBus;

  /**
   * Create a new Backplane and get the Signal and Bus objects.
   */
  @BeforeEach
  void beforeEach() {
    backplane = new Backplane();
    clock = new Clock(backplane.clock());
    addressBus = backplane.address();
    dataBus = backplane.data();
    rwb = backplane.rwb();
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
    clock.cycle();
    return (byte)dataBus.value();
  }

  /**
   * Check to see if the read retuns the expected value.
   *
   * @param address the address to check
   * @param expectSuccess true if the test should expect the 0xAA value
   *                      from the address.
   */
  private void check(int address, boolean expectSuccess) {
    dataBus.value((byte)0xEE);
    assertEquals(expectSuccess ? (byte)0xAA : (byte)0xEE, read(address));
  }

  /**
   * Create a temporary file filed with 0xAA bytes.
   *
   * @param size the size of the file.
   * @return the Path to the file
   * @throws IOException if the file cannot be created.
   */
  private Path createFile(int size) throws IOException {
    Path path = Files.createTempFile("test",".bin");
    try(OutputStream out = Files.newOutputStream(path)) {
      byte[] values = new byte[size];
      Arrays.fill(values, (byte)0xAA);
      out.write(values);
    }
    return path;
  }

  /**
   * Test an 8k ROM configuration, starting at address 0xE000.
   */
  @Test
  void test8k() throws IOException {
    Path path = null;
    try {
      path = createFile(0x2000);
      Map<String, String> options = Map.of(
          "base", "E000",
          "size", "2000",
          "file", path.toString());
      new ROM(backplane, options);

      check(0xDFFF, false);
      check(0xE000, true);
      check(0xFFFF, true);
    }
    finally {
      if(path != null) {
        Files.delete(path);
      }
    }
  }
}
