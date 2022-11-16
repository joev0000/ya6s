/* Copyright (C) 2021, 2022 Joseph Vigneau */

package joev.ya6s.signals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A signal in a digital system.
 *
 * Can be true or false, does not support a high-impedance mode.
 */
public class Signal {
  /**
   * Types of events that can be sent to Listeners.
   */
  public enum EventType {
    NEGATIVE_EDGE,
    POSITIVE_EDGE
  }

  /**
   * Interface for objects that want to be notified of Signal events.
   */
  @FunctionalInterface
  public interface Listener {
    /**
     * Handle an event.
     *
     * @param type the type of the event
     */
    void event(EventType type);
  }

  private final String name;
  private Listener[] listeners = new Listener[8];
  private int listenerCount = 0;

  private boolean value;

  /**
   * Create a new Signal with the given name.
   *
   * @param name the name of the Signal.
   */
  public Signal(String name) {
    this.name = name;
  }

  /**
   * Get the name of the Signal.
   *
   * @return name the name of the Signal.
   */
  public String name() { return name; }

  /**
   * Get the value of the Signal.
   *
   * @return the value of the Signal.
   */
  public boolean value() { return value; }

  /**
   * Set the value of the signal.
   *
   * If the value changes, listeners to be notified.
   *
   * @param value the new value of the Signal.
   */
  public void value(boolean value) {
    boolean oldValue = this.value;
    this.value = value;
    edgeNotify(oldValue, value);
  }

  protected void edgeNotify(boolean oldValue, boolean newValue) {
    EventType eventType = null;
    if(!oldValue && newValue) {
      eventType = EventType.POSITIVE_EDGE;
    }
    else if(oldValue && !newValue) {
      eventType = EventType.NEGATIVE_EDGE;
    }
    if(eventType != null) {
      for(int i = 0; i < listenerCount; i++) {
        listeners[i].event(eventType);
      }
    }
  }

  /**
   * Register a Listener.
   *
   * @param listener a Listener to notify when the Signal changes.
   */
  public void register(Listener listener) {
    if(listenerCount == listeners.length) {
      Listener[] newListeners = new Listener[listenerCount + 8];
      System.arraycopy(listeners, 0, newListeners, 0, listenerCount);
      listeners = newListeners;
    }
    listeners[listenerCount] = listener;
    listenerCount++;
  }

  /**
   * Unregister a Listener.
   *
   * @param listener a Listener to no longer notify.
   */
  public void unregister(Listener listener) {
    for(int i = 0; i < listenerCount; i++) {
      if(listeners[i] == listener) {
        System.arraycopy(listeners, i+1, listeners, i, listenerCount - i - 1);
        listenerCount--;
        break;
      }
    }
  }

  /**
   * Get a human-readable String representation of the Signal.
   *
   * @return a human-readable representation of the Signal.
   */
  @Override
  public String toString() {
    return String.format("%s: %b", name, value);
  }
}
