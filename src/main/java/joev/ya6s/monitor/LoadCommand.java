package joev.ya6s.monitor;

import joev.ya6s.Backplane;
import joev.ya6s.W65C02S;
import joev.ya6s.signals.Bus;
import joev.ya6s.signals.Signal;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Command to load the contents of a file into the Backplane
 */
public class LoadCommand implements Command {
  private final short start;
  private final String path;

  /**
   * Create a load command for the given start location, from the path.
   *
   * @param start the starting location to load the data.
   * @param path a file path containing data to load.
   */
  public LoadCommand(short start, String path) {
    this.start = start;
    this.path = path;
  }

  /**
   * Load the file contents into memory.
   *
   * @param monitor the Monitor which will execute this command.
   * @return a suggested next Command.  Null.
   */
  @Override
  public Command execute(Monitor monitor) {
    Backplane backplane = monitor.backplane();
    W65C02S cpu = monitor.cpu();
    Signal rdy = cpu.rdy();
    Signal clock = backplane.clock();
    Signal rwb = backplane.rwb();
    Bus address = backplane.address();
    Bus dataBus = backplane.data();

    boolean oldRdy = rdy.value();
    try(InputStream in = new FileInputStream(path)) {
      rdy.value(false);
      short loc = start;
      byte[] data = in.readAllBytes();
      for(int i = 0; i < data.length; i++) {
        clock.value(false);
        address.value(loc++);
        dataBus.value(data[i]);
        rwb.value(false);
        clock.value(true);
      }
    }
    catch (IOException ioe) {
      System.out.format("error: %s%n", ioe.getMessage());
    }
    finally {
      rdy.value(oldRdy);
    }
    return null;
  }

  /**
   * Return a human-readable representation of this command.
   *
   * @return "load {start} {path}"
   */
  @Override
  public String toString() { 
    return String.format("load %04X %s%n", start, path);
  }
}
