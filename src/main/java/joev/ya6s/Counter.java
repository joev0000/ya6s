package joev.ya6s;

import java.util.Map;

import joev.ya6s.signals.Bus;
import joev.ya6s.signals.Signal;

/**
 * 24-bit counter that counts down each clock cycle.
 *
 * reg 0-2: counter value (24 bits, little-endian)
 * reg 3: control/status
 *   bit 0: Counter Enable
 *   bit 1: reserved
 *   bit 2: Interrupt Enable
 *   bit 3: reserved
 *   bit 4: reserved
 *   bit 5: reserved
 *   bit 6: Zero (can be read with BIT)
 *   bit 7: reserved for Error
 *
 * Each click rising edge decrements the counter value, if counter enabled.
 *
 * Sample program that counts down from one million, then halts.  The counter
 * base address is $F100.
 * <code>
 * COUNTR  :=      $F100           ; Counter base address
 * C_LO    :=      COUNTR + 0      ; Low bits of counter
 * C_MID   :=      COUNTR + 1      ; Middle bits of counter
 * C_HI    :=      COUNTR + 2      ; High bits of counter
 * C_CTRL  :=      COUNTR + 3      ; Counter control register
 *
 * START:  LDA     C_CTRL          ; A = Counter control
 *         AND     #$FA            ; Clear counter and interrupt enable bits
 *         STA     C_CTRL          ; Store counter control
 *
 *         LDA     #$40            ; A = Low bits of 1000000(10)
 *         STA     C_LO            ; Store them in counter low
 *         LDA     #$42            ; A = Middle bits of 1000000(10)
 *         STA     C_MID           ; Store them in counter mid
 *         LDA     #$0F            ; A = High bits of 1000000(10)
 *         STA     C_HI            ; Store them in counter high
 *
 *         LDA     C_CTRL          ; A = Counter control
 *         ORA     #$01            ; set counter enable bit
 *         STA     C_CTRL          ; Store counter control
 *
 * LOOP:   BIT     C_CTRL          ; Check the counter control bits
 *         BVC     LOOP            ; If bit 6 (counter zero) not set, loop.
 *
 *         STP                     ; Halt
 * </code>
 */
public class Counter {
  private final Backplane backplane;
  private final short baseAddress;
  private final Signal.Listener tickFn;

  private final Bus address;
  private final Bus data;
  private final Signal rwb;

  private int counter = 0;
  private byte control = (byte) 0;

  private static final byte COUNTER_ENABLE = 0x01;
  private static final byte INTERRUPT_ENABLE = 0x04;
  private static final byte ZERO = 0x40;

  /**
   * Create a new 24-bit counter device.
   *
   * @param backplane the backplane to attach.
   * @param options the options for the counter. "base" is the base address.
   */
  public Counter(Backplane backplane, Map<String, String> options) {
    if(!options.containsKey("base")) {
      throw new IllegalArgumentException("Missing \"base\" option.");
    }
    this.backplane = backplane;

    baseAddress = (short)Integer.parseUnsignedInt(options.get("base"), 16);
    tickFn = this::tick;

    address = backplane.address();
    data = backplane.data();
    rwb = backplane.rwb();

    backplane.clock().register(tickFn);
  }

  /**
   * Process a tick.  Decrements counter on each rising clock edge if the
   * counter enable bit is set.  Sets the zero bit if the counter is zero.
   */
  private void tick(Signal.EventType eventType) {
    if (eventType != Signal.EventType.POSITIVE_EDGE) {
      return;
    }
    if ((control & COUNTER_ENABLE) != 0) {
      counter--;
    }
    if (counter == 0) {
      control |= ZERO;
      control &= ~COUNTER_ENABLE;
      if((control & INTERRUPT_ENABLE) != 0) {
        backplane.irqb().value(this, false);
      }
    }
    short addressMask = ~0x3;
    if ((short) (address.value() & addressMask) != baseAddress) {
      return;
    }
    int reg = address.value() & ~addressMask;

    if (rwb.value()) {
      switch (reg) {
        case 0 -> data.value((byte) (counter & 0xFF));
        case 1 -> data.value((byte) ((counter >> 8) & 0xFF));
        case 2 -> data.value((byte) ((counter >> 16) & 0xFF));
        case 3 -> data.value(control);
        default -> {
        }
      }
    } else {
      switch (reg) {
        case 0:
          if ((control & COUNTER_ENABLE) == 0) {
            counter = ((counter & ~0xFF) | (data.value() & 0xFF));
          }
          break;
        case 1:
          if ((control & COUNTER_ENABLE) == 0) {
            counter = ((counter & ~0xFF00) | ((data.value() & 0xFF) << 8));
          }
          break;
        case 2:
          if ((control & COUNTER_ENABLE) == 0) {
            counter = ((counter & ~0xFF0000) | ((data.value() & 0xFF) << 16));
          }
          break;
        case 3:
          control = (byte) (data.value() & (COUNTER_ENABLE | INTERRUPT_ENABLE));
          break;
        default:
      }
      if(counter == 0) {
        control |= ZERO;
      }
      else {
        control &= ~ZERO;
      }
      backplane.irqb().value(this, !(((control & INTERRUPT_ENABLE) != 0) && (counter == 0)));
    }
  }
}
