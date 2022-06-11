/* Copyright (C) 2021, 2022 Joseph Vigneau */

package joev.ya6s;

import java.util.Map;

import joev.ya6s.signals.Bus;
import joev.ya6s.signals.Signal;

/**
 * An implementation of a static RAM module.
 */
public class SRAM {
  private final Bus address;
  private final Bus data;
  private final Signal rwb;
  private final Signal clock;
  private final Signal.Listener tickFn;
  private final int base;
  private final int end;
  private final byte[] memory;

  /**
   * Create a Static RAM module on the backplane that covers the entire
   * address range.
   *
   * @param backplane the backplane to attach to.
   */
  public SRAM(Backplane backplane) {
    this(backplane, (short)0, 0x10000);
  }

  /**
   * Create a Static RAM module with the given busses and signals.
   *
   * @param backplane the backplane to attach to.
   * @param baseAddress the base address of the SRAM.
   * @param size the size of the SRAM.
   */
  public SRAM(Backplane backplane, short baseAddress, int size) {
    this(backplane, Map.of(
      "base", Integer.toHexString(baseAddress & 0xFFFF),
      "size", Integer.toHexString(size)));
  }
  /**
   * Create a Static RAM module with the given busses and signals.
   *
   * @param backplane the backplane to attach to.
   * @param options a Map containing the configuration options:
   *   "base" is the hex value of the base address.
   *   "size" is the hex value of the size.
   */
  public SRAM(Backplane backplane, Map<String, String> options) {
    String baseString = options.get("base");
    String sizeString = options.get("size");

    if(baseString == null || sizeString == null) {
      throw new IllegalArgumentException("Both \"base\" and \"size\" options are required.");
    }

    base = Integer.parseUnsignedInt(baseString, 16);
    int size = Integer.parseUnsignedInt(sizeString, 16);

    address = backplane.address();
    data = backplane.data();
    rwb = backplane.rwb();
    clock = backplane.clock();
    this.end = base + size - 1;

    memory = new byte[size];

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
      int busAddress = address.value() & 0xFFFF;
      if(busAddress >= base && busAddress <= end) {
        if(rwb.value()) {
          data.value(memory[busAddress - base]);
        }
        else {
          memory[busAddress - base] = (byte)data.value();
        }
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
