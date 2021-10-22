package joev.ya6s;

import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import joev.ya6s.signals.Bus;
import joev.ya6s.signals.Signal;

/**
 * An implementation of a ROM module.
 */
public class ROM {
  private final Bus address;
  private final Bus data;
  private final Signal rwb;
  private final Signal clock;
  private final Signal.Listener tickFn;
  private final short maskedAddress;
  private final short mask;
  private final short base;
  private final byte[] memory;

  /**
   * Create a ROM module with the given busses and signals.
   *
   * @param backplane the backplane to attach to.
   * @param options a Map containing the configuration options:
   *   "base" is the hex value of the base address.
   *   "mask" is the hex value of the address mask.
   *   "file" is the path to the contents that will be loaded into the ROM.
   */
  public ROM(Backplane backplane, Map<String, String> options) {
    String baseString = options.get("base");
    String maskString = options.get("mask");
    String fileName   = options.get("file");

    if(baseString == null || maskString == null) {
      throw new IllegalArgumentException("Both \"base\" and \"mask\" options are required.");
    }

    Path path = Path.of(fileName);
    if(!Files.exists(path)) {
      throw new IllegalArgumentException("File does not exist.");
    }
    try(InputStream in = Files.newInputStream(path)) {
      memory = in.readAllBytes();
    }
    catch (IOException ioe) {
      throw new IllegalArgumentException("Could not read file.");
    }

    short base = (short)Integer.parseUnsignedInt(baseString, 16);
    short mask = (short)Integer.parseUnsignedInt(maskString, 16);

    address = backplane.address();
    data = backplane.data();
    rwb = backplane.rwb();
    clock = backplane.clock();
    this.base = base;
    this.mask = mask;
    this.maskedAddress = (short)(base & mask);

    tickFn = this::tick;
    clock.register(tickFn);
  }

  /**
   * Handle a clock tick.  If this is a positive  transition, read or write from the data bus.
   *
   * @param eventType the type of the Signal event.
   */
  private void tick(Signal.EventType eventType) {
    if(eventType == Signal.EventType.POSITIVE_EDGE) {
      short busAddress = (short)address.value();
      if(rwb.value() && (short)(busAddress & mask) == maskedAddress) {
        data.value(memory[(busAddress - base) & 0xFFFF]);
      }
    }
  }

  /**
   * Unregister from the clock Signal.
   */
  public void close() {
    clock.unregister(tickFn);
  }
}
