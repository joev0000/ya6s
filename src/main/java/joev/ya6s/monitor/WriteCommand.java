/* Copyright (C) 2021, 2022 Joseph Vigneau */

package joev.ya6s.monitor;

import joev.ya6s.Backplane;
import joev.ya6s.Clock;
import joev.ya6s.signals.Bus;
import joev.ya6s.signals.Signal;

/**
 * Command to write data into the Backplane.
 */
public class WriteCommand implements Command {
  private final short start;
  private final byte[] data;
  /**
   * Create a new write command with the given start location and data.
   *
   * @param start the start location of where the data is loaded.
   * @param data the data to load at the start location.
   */
  public WriteCommand(short start, byte[] data) {
    this.start = start;
    this.data = data.clone();
  }

  /**
   * Load the data into the Backplane.
   *
   * @param monitor the Monitor to execute this command against.
   * @return a suggested next Command
   */
  @Override
  public Command execute(Monitor monitor) {
    Backplane backplane = monitor.backplane();
    Clock clock = monitor.clock();

    Signal rdy = backplane.rdy();
    Signal rwb = backplane.rwb();
    Bus address = backplane.address();
    Bus dataBus = backplane.data();

    boolean oldRdy = rdy.value();
    rdy.value(false);
    short loc = start;
    for (byte datum : data) {
      address.value(loc++);
      dataBus.value(datum);
      rwb.value(false);
      clock.cycle();
    }
    rdy.value(oldRdy);
    return null;
  }

  /**
   * Return a human-readable representation of this command.
   *
   * @return "write {start} {byte}*"
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("write %04X", start));
    for (byte datum : data) {
      sb.append(String.format(" %02X", datum));
    }
    return sb.toString();
  }
}
