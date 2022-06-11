/* Copyright (C) 2021, 2022 Joseph Vigneau */

package joev.ya6s.signals;

import java.util.HashMap;
import java.util.Map;

/**
 * A type of Signal that simulates an open collector circuit.  Components can
 * update the open collector with their value, and the value of the open
 * collector is true only if all input values are true.  Components can
 * and should detach themselves when they no longer participate in the
 * circuit.
 */
public class OpenCollector extends Signal {
  private final Map<Object, Boolean> map = new HashMap<>();

  /**
   * Create a new OpenCollector with the given name.
   *
   * @param name the name of the open collector
   */
  public OpenCollector(String name) {
    super(name);
  }

  /**
   * Get the value of the open collector.
   *
   * @return true only if all input values are true.
   */
  @Override
  public boolean value() {
    for(Boolean b: map.values()) {
      if(!b) {
        return false;
      }
    }
    return true;
  }

  /**
   * Do nothing.  This overrides the behavior inherited from Signal.
   *
   * TODO: Change the object hierarchy to not require this.
   */
  @Override
  public void value(boolean value) {
  }

  /**
   * Set an input value to the open collector.
   *
   * This is used by a component to declare its value:
   *
   *     oc.value(this, true);
   *
   * @param o the input component.
   * @param value the value to set for that component.
   */
  public void value(Object o, boolean value) {
    boolean oldValue = value();
    map.put(o, value);
    edgeNotify(oldValue, value());
  }

  /**
   * Detach a component from the open collector.
   *
   * @param o the input component to dispatch.
   */
  public void detach(Object o) {
    map.remove(o);
  }
}

