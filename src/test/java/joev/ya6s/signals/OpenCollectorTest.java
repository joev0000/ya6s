package joev.ya6s.signals;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class OpenCollectorTest {
  @Test
  void noAttachmentsIsTrue() {
    OpenCollector oc = new OpenCollector("test");

    assertTrue(oc.value());
  }

  @Test
  void allTrueIsTrue() {
    OpenCollector oc = new OpenCollector("test");
    Object object1 = new Object();
    Object object2 = new Object();
    Object object3 = new Object();

    oc.value(object1, true);
    oc.value(object2, true);
    oc.value(object3, true);

    assertTrue(oc.value());
  }

  @Test
  void anyFalseIsFalse() {
    OpenCollector oc = new OpenCollector("test");
    Object object1 = new Object();
    Object object2 = new Object();
    Object object3 = new Object();

    oc.value(object1, true);
    oc.value(object2, false);
    oc.value(object3, true);

    assertFalse(oc.value());
  }
}
