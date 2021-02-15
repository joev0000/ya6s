package joev.ya6s.monitor;

import joev.ya6s.Backplane;
import joev.ya6s.W65C02;
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
    this.data = data;
  }

  /**
   * Load the data into the Backplane.
   *
   * @param backplane the Backplane to load the data into.
   * @param cpu the CPU to unready while the data is being loaded.
   * @return a suggested next Command
   */
  @Override
  public Command execute(Backplane backplane, W65C02 cpu) {
    Signal rdy = cpu.rdy();
    Signal clock = backplane.clock();
    Signal rwb = backplane.rwb();
    Bus address = backplane.address();
    Bus dataBus = backplane.data();

    boolean oldRdy = rdy.value();
    rdy.value(false);
    short loc = start;
    for(int i = 0; i < data.length; i++) {
      clock.value(false);
      address.value(loc++);
      dataBus.value(data[i]);
      rwb.value(false);
      clock.value(true);
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
    for(int i = 0; i < data.length; i++) {
      sb.append(String.format(" %02X", data[i]));
    }
    return sb.toString();
  }
}
