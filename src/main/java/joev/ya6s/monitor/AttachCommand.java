/* Copyright (C) 2021, 2022 Joseph Vigneau */

package joev.ya6s.monitor;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Objects;

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
    this.options = Map.copyOf(options);
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

  /**
   * Compare this AttachCommand with another Object.
   *
   * @param other the other Object to compare
   * @return true if the other Object is an AttachCommand with the same value.
   */
  @Override
  public boolean equals(Object other) {
    if(other instanceof AttachCommand o) {
      return this.typeName.equals(o.typeName) && this.options.equals(o.options);
    }
    return false;
  }

  /**
   * Return the hash code of this AttachCommand.
   *
   * @return the hash code of this AttachCommand.
   */
  @Override
  public int hashCode() {
    return Objects.hash(this.typeName, this.options);
  }
}
