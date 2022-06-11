/* Copyright (C) 2021, 2022 Joseph Vigneau */

package joev.ya6s.signals;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class SignalTests {
  @Test
  void basic() {
    Signal s = new Signal("test");
    s.value(true);
    assertEquals("test", s.name());
    assertTrue(s.value());
  }

  @Test
  void detectPositiveEdge() {
    Signal s = new Signal("test");
    s.value(false);
    Set<Signal.EventType> types = new HashSet<>();
    s.register(eventType -> types.add(eventType));

    s.value(true);
    assertTrue(types.contains(Signal.EventType.POSITIVE_EDGE));
    assertFalse(types.contains(Signal.EventType.NEGATIVE_EDGE));
  }

  @Test
  void detectNegativeEdge() {
    Signal s = new Signal("test");
    s.value(true);
    Set<Signal.EventType> types = new HashSet<>();
    s.register(eventType -> types.add(eventType));

    s.value(false);
    assertTrue(types.contains(Signal.EventType.NEGATIVE_EDGE));
    assertFalse(types.contains(Signal.EventType.POSITIVE_EDGE));
  }
}
