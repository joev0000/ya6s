/* Copyright (C) 2021, 2022 Joseph Vigneau */

package joev.ya6s.monitor;

import java.lang.reflect.Constructor;
import java.util.Map;

import joev.ya6s.Backplane;

/**
 * Command to attach a device to a Backplane
 */
public class AttachCommand implements Command {
  private final String typeName;
  private final Map<String, String> options;

  /**
   * Create an attach command with the given type and options.
   *
   * @param typeName a Java class name that has a constructor that takes
   *   an argument list of (Backplane, Map&lt;String, String&gt;)
   * @param options the options to pass into the newly created device.
   */
  public AttachCommand(String typeName, Map<String, String> options) {
    this.typeName = typeName;
    this.options = options;
  }

  /**
   * Create a new instance of the device.
   *
   * @param monitor the Monitor which will execute this command.
   * @return a suggested next Command.  Null.
   */
  @Override
  public Command execute(Monitor monitor) {
    try {
      Class<?> cls = Class.forName(typeName);
      Constructor<?> constructor = cls.getConstructor(Backplane.class, Map.class);
      constructor.newInstance(monitor.backplane(), options);
    }
    catch(Exception e) {
      System.out.format("error: %s: %s%n", e.getClass().getName(), e.getMessage());
    }
    return null;
  }
}
