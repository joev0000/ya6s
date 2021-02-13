package joev.ya6s;

import joev.ya6s.signals.Bus;
import joev.ya6s.signals.Signal;

/**
 * A Backplane holds the signls and busses used throughout the system.
 */
public class Backplane {
  private final Bus address;
  private final Bus data;
  private final Signal rwb;
  private final Signal sync;
  private final Signal clock;

  /**
   * Create a new Backplane
   */
  public Backplane() {
    address = new Bus("address", 16);
    data = new Bus("data", 8);
    rwb = new Signal("rwb");
    sync = new Signal("sync");
    clock = new Signal("clock");
  }

  /**
   * Get the address bus.
   *
   * @return the address bus.
   */
  public Bus address() { return address; }

  /**
   * Get the data bus.
   *
   * @return the data bus.
   */
  public Bus data() { return data; }

  /**
   * Get the rwb (Read, not Write) signal.
   *
   * @return the rwb signal.
   */
  public Signal rwb() { return rwb; }

  /**
   * Get the sync signal, which is true while an opcode is being read.
   *
   * @return the sync signal
   */
  public Signal sync() { return sync; }

  /**
   * Get the clock signal.
   *
   * @return the clock signal.
   */
  public Signal clock() { return clock; }
}

