package joev.ya6s.monitor;

import java.lang.reflect.Constructor;
import java.util.Map;

import joev.ya6s.Backplane;
import joev.ya6s.W65C02;

/**
 * Command to attach a device to a Backplane
 */
public class AttachCommand implements Command {
  private String typeName;
  private Map<String, String> options;

  /**
   * Create an attach command with the given type and options.
   *
   * @param typeName a Java class name that has a constuctor that takes
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
   * @param backplane the Backplane to attach the device.
   * @param cpu the CPU.  Unused.
   * @return a suggested next Command.  Null.
   */
  @Override
  public Command execute(Backplane backplane, W65C02 cpu) {
    try {
      Class<?> cls = Class.forName(typeName);
      Constructor<?> ctor = cls.getConstructor(Backplane.class, Map.class);
      ctor.newInstance(backplane, options);
    }
    catch(Exception e) {
      System.out.format("error: %s: %s%n", e.getClass().getName(), e.getMessage());
    }
    return null;
  }
}
