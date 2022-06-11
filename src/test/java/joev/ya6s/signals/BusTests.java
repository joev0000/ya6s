/* Copyright (C) 2021, 2022 Joseph Vigneau */

package joev.ya6s.signals;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class BusTests {
  @Test
  void basic() {
    Bus bus = new Bus("test", 16);
    bus.value(0x1234);
    assertEquals(16, bus.width());
    assertEquals(0x1234, bus.value());
  }

  @Test
  void clampTo16() {
    Bus bus = new Bus("test", 16);
    bus.value(0x12345678);
    assertEquals(16, bus.width());
    assertEquals(0x5678, bus.value());
  }

  @Test
  void clampTo8() {
    Bus bus = new Bus("test", 8);
    bus.value(0x12345678);
    assertEquals(8, bus.width());
    assertEquals(0x78, bus.value());
  }

  @Test
  void clampTo32() {
    Bus bus = new Bus("test", 128);
    assertEquals(32, bus.width());
  }
}
