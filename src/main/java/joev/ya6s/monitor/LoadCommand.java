/* Copyright (C) 2021, 2022 Joseph Vigneau */

package joev.ya6s.monitor;

import joev.ya6s.Backplane;
import joev.ya6s.Clock;
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
    Signal rdy = backplane.rdy();
    Signal rwb = backplane.rwb();
    Bus address = backplane.address();
    Bus dataBus = backplane.data();

    Clock clock = monitor.clock();
    boolean oldRdy = rdy.value();
    try(InputStream in = new FileInputStream(path)) {
      rdy.value(false);
      short loc = start;
      byte[] data = in.readAllBytes();
      for (byte datum : data) {
        address.value(loc++);
        dataBus.value(datum);
        rwb.value(false);
        clock.cycle();
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
