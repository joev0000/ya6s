package joev.ya6s;

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
   * Create a Static RAM module that takes up the entire address space.
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
    address = backplane.address();
    data = backplane.data();
    rwb = backplane.rwb();
    clock = backplane.clock();
    this.mask = mask;
    this.maskedAddress = (short)(baseAddress & mask);

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
        }
        else {
          memory[busAddr & 0xFFFF] = (byte)data.value();
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
