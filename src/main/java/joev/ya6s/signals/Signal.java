package joev.ya6s.signals;

import java.util.HashSet;
import java.util.Set;

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
    public void event(EventType type);
  }

  private final String name;
  private final Set<Listener> listeners = new HashSet<>();
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
    EventType eventType = null;
    if(!this.value && value) {
      eventType = EventType.POSITIVE_EDGE;
    }
    else if(this.value && !value) {
      eventType = EventType.NEGATIVE_EDGE;
    }
    this.value = value;
    if(eventType != null) {
      for(Listener listener: listeners) {
        listener.event(eventType);
      }
    }
  }

  /**
   * Register a Listener.
   *
   * @param listener a Listener to notify when the Signal changes.
   */
  public void register(Listener listener) {
    listeners.add(listener);
  }

  /**
   * Unregister a Listener.
   *
   * @param listener a Listener to no longer notify.
   */
  public void unregister(Listener listener) {
    listeners.remove(listener);
  }

  /**
   * Get a human-readable String representation of the Signal.
   *
   * @return a human-readable represenation of the Signal.
   */
  @Override
  public String toString() {
    return String.format("%s: %b", name, value);
  }
}