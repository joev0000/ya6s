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
  private final short maskedAddress;
  private final short mask;
  private final byte[] memory = new byte[0x10000];

  /**
   * Create a Static RAM module on the backplane that covers the entire
   * address range.
   *
   * @param backplane the backplane to attach to.
   */
  public SRAM(Backplane backplane) {
    this(backplane, (short)0, (short)0);
  }

  /**
   * Create a Static RAM module with the given busses and signals.
   *
   * @param backplane the backplane to attach to.
   * @param baseAddress the base address of the SRAM.
   * @param mask the address mask of the SRAM.
   */
  public SRAM(Backplane backplane, short baseAddress, short mask) {
    this(backplane, Map.of(
      "base", Integer.toHexString(baseAddress & 0xFFFF),
      "mask", Integer.toHexString(mask & 0xFFFF)));
  }
  /**
   * Create a Static RAM module with the given busses and signals.
   *
   * @param backplane the backplane to attach to.
   * @param options a Map containing the configuration options:
   *   "base" is the hex value of the base address.
   *   "mask" is the hex value of the address mask.
   */
  public SRAM(Backplane backplane, Map<String, String> options) {
    String baseString = options.get("base");
    String maskString = options.get("mask");

    if(baseString == null || maskString == null) {
      throw new IllegalArgumentException("Both \"base\" and \"mask\" options are required.");
    }

    short base = (short)Integer.parseUnsignedInt(baseString, 16);
    short mask = (short)Integer.parseUnsignedInt(maskString, 16);

    address = backplane.address();
    data = backplane.data();
    rwb = backplane.rwb();
    clock = backplane.clock();
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
      short busAddr = (short)address.value();
      if((short)(busAddr & mask) == maskedAddress) {
        if(rwb.value()) {
          data.value(memory[busAddr & 0xFFFF]);
          /*
          if((busAddr & 0xFF00) == 0x0000 ||
             (busAddr & 0xFF00) == 0x0100 || 
             (busAddr & 0xFF00) == 0x0200) {
            System.out.format("SRAM: load  %04X: %02X%n", busAddr, data.value());
          }
          */
        }
        else {
          memory[busAddr & 0xFFFF] = (byte)data.value();
          /*
          if((busAddr & 0xFF00) == 0x0000 ||
             (busAddr & 0xFF00) == 0x0100 || 
             (busAddr & 0xFF00) == 0x0200) {
            System.out.format("SRAM: store %04X: %02X%n", busAddr, data.value());
          }
          */
        }
      }
    }
  }

  /**
   * Deregister from the clock Signal.
   */
  public void close() {
    clock.unregister(tickFn);
  }
}
