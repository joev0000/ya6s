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
  private final byte[] memory = new byte[0x10000];

  /**
   * Create a Static RAM module with the given busses and signals.
   *
   * @param address A 16-bit address bus.
   * @param data An 8-bit data bus.
   * @param rwb A signal that determines if data should be written to the bus (true) or read from it (false)
   * @param clock The clock signal, read or write memory upon a negative clock transition.
   */
  public SRAM(Backplane backplane) {
    //Bus address, Bus data, Signal rwb, Signal clock) {
    address = backplane.address();
    data = backplane.data();
    rwb = backplane.rwb();
    clock = backplane.clock();

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
      // check address.value to see if we're selected.
      if(rwb.value()) {
        data.value(memory[address.value()]);
      }
      else {
        memory[address.value()] = (byte)data.value();
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
