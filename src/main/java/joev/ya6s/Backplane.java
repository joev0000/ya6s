package joev.ya6s;

import joev.ya6s.signals.Bus;
import joev.ya6s.signals.OpenCollector;
import joev.ya6s.signals.Signal;

/**
 * A Backplane holds the signals and busses used throughout the system.
 */
public class Backplane {
  private final Bus address;
  private final Bus data;
  private final Signal rwb;
  private final Signal sync;
  private final Signal clock;
  private final Signal vpb;
  private final Signal mlb;
  private final Signal be;
  private final OpenCollector irqb;
  private final OpenCollector nmib;
  private final Signal resb;
  private final Signal rdy;

  /**
   * Create a new Backplane
   */
  public Backplane() {
    address = new Bus("address", 16);
    data = new Bus("data", 8);
    rwb = new Signal("rwb");
    sync = new Signal("sync");
    clock = new Signal("clock");
    vpb = new Signal("vpb");
    mlb = new Signal("mlb");
    be = new Signal("be");
    irqb = new OpenCollector("irqb");
    rdy = new Signal("rdy");
    resb = new Signal("resb");
    nmib = new OpenCollector("nmib");
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

  /**
   * Get the vector pull signal.
   *
   * @return the vector pull signal.
   */
  public Signal vpb() { return vpb; }

  /**
   * Get the memory lock signal.
   *
   * @return the memory lock signal.
   */
  public Signal mlb() { return mlb; }

  /**
   * Get the bus enable signal.
   *
   * @return the bus enable signal.
   */
  public Signal be() { return be; }

  /**
   * Get the irqb open collector signal.
   *
   * @return the irqb signal.
   */
  public OpenCollector irqb() { return irqb; }

  /**
   * Get the nmiq open collector signal.
   *
   * @return the nmib signal.
   */
  public OpenCollector nmib() { return nmib; }

  /**
   * Get the reset signal.
   *
   * @return the reset signal
   */
  public Signal resb() { return resb; }

  /**
   * Get the ready signal.
   *
   * @return the ready signal.
   */
  public Signal rdy() { return rdy; }
}

