/* Copyright (C) 2021, 2022 Joseph Vigneau */

package joev.ya6s;

import joev.ya6s.monitor.Monitor;
import joev.ya6s.signals.Bus;
import joev.ya6s.signals.Signal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * Module that simulates a 16550D-based UART.
 *
 * Divisor values for common baud rates:
 *
 *   Baud   DLL DLM
 *    110   $E9 $28
 *    300   $00 $0F
 *   1200   $98 $03
 *   2400   $E0 $01
 *   9600   $78 $00
 *  19200   $3C $00
 *  56000   $15 $00
 * 128000   $09 $00
 *
 * Sample program to echo bytes at 9600 baud with a UART at $F000:
 * <code>
 *         LDA #$83    ; A = Set Divisor Latch Access Bit, 8-N-1
 *         STA $F003   ; Line Control Register = A
 *         LDA #$78    ; A = Divisor Latch Low bits
 *         STA $F000   ; Divisor Latch Least Significant Bits = A
 *         STZ $F001   ; Divisor Latch Most Significant Bits = 0
 *         LDA #$03    ; A = Clear Divisor Latch Access Bit, 8-N-1
 *         STA $F003   ; Line Control Register = A
 *
 * LOOP    LDA #$01    ; Set Data Ready bit
 * RLOOP   BIT $F005   ; Check Line Status Register
 *         BEQ RLOOP   ; If not set, try again.
 *         LDX $F000   ; X = Receive Buffer Register
 *         LDA #$20    ; Set the Transmitter Holding Register Empty bit.
 * WLOOP   BIT $F005   ; Check Line Status Register
 *         BEQ WLOOP   ; If not set, try again.
 *         STX $F000   ; Transmitter Holding Register = X
 *         BRA LOOP    ; Read the next byte
 *
 * ; A9 83 8D 03 F0 A9 78 8D 00 F0 9C 01 F0 A9 03 8D 03 F0 A9 01 2C 05 F0 F0 FB AE 00 F0 A9 20 2C 05 F0 F0 FB 8E 00 F0 80 EA
 *
 * </code>
 */
public final class UART {
  private final static short addressMask = (short)0xFFF8;
  private final static double frequency = 18432000d; // 18.432MHz crystal

  private final Backplane backplane;

  private final Bus address;
  private final Bus data;
  private final Signal.Listener tickFn;
  private final short baseAddress;

  private final Thread xmitThread;
  private final byte[] xmitFifo = new byte[16];
  private int xmitHead = 0;
  private int xmitTail = 0;

  private final Thread recvThread;
  private final byte[] recvFifo = new byte[16];
  private int recvHead = 0;
  private int recvTail = 0;

  private int delayMillis = 0;
  private int delayNanos = 0;

  private final InputStream in;
  private final OutputStream out;
  private boolean closing = false;

  // 16550D Registers

  /** Receiver Buffer Register */
  private byte RBR = 0;

  /** Transmitter Holding Register */
  private byte THR = 0;

  /** Interrupt Enable Register */
  private byte IER = 0;

  /** Interrupt Identification Register */
  private byte IIR = 1;

  /** FIFO Control Register */
  private byte FCR = 0;

  /** Line Control Register */
  private byte LCR = 0;

  /** MODEM Control Register */
  private byte MCR = 0;

  /** Line Status Register */
  private byte LSR = 0x60;

  /** MODEM Status Register */
  private byte MSR = 0;

  /** Scratch Register */
  private byte SCR = 0;

  /** Divisor Latch (Least Significant) */
  private byte DLL = 0;

  /** Divisor Latch (Most Significant) */
  private byte DLM = 0;

  // LSR bits

  /** Data Ready */
  private final static byte DR   = 0x01;

  /** Overrun Error */
  private final static byte OE   = 0x02;

  /** Parity Error */
  private final static byte PE   = 0x04;

  /** Framing Error */
  private final static byte FE   = 0x08;

  /** Break Interrupt */
  private final static byte BI   = 0x10;

  /** Transmitter Holding Register Empty */
  private final static byte THRE = 0x20;

  /** Transmitter Empty */
  private final static byte TEMT = 0x40;

  /** Error in Receiver FIFO */
  private final static byte LSR7 = (byte)0x80;

  /**
   * Create a UART on the given Backplane, with the given base address and I/O streams.
   *
   * @param backplane the Backplane containing the address and data busses.
   * @param options the Map of options for this UART.
   *   "base": the hex address of the base of the UART registers.
   *   "port" (future): the decimal port number to listen on, or "tty" if the
   *     monitor input and output should be used.
   */
  public UART(Backplane backplane, Map<String, String> options) {
    short base;
    if(!options.containsKey("base")) {
      throw new IllegalArgumentException("Missing \"base\" option.");
    } else {
      base = (short)Integer.parseInt(options.get("base"), 16);
    }

    if(!options.containsKey("port") || "tty".equalsIgnoreCase(options.get("port"))) {
      this.in = Monitor.ttyIn;
      this.out = Monitor.ttyOut;
    } else {
      throw new IllegalArgumentException("Non-\"tty\" value for \"port\" option not yet supported.");
    }

    this.backplane   = backplane;
    this.baseAddress = base;

    address = backplane.address();
    data    = backplane.data();

    tickFn = this::tick;
    backplane.clock().register(tickFn);

    xmitThread = new Thread(this::transmitter, String.format("UART $%04X transmitter", baseAddress));
    xmitThread.start();

    recvThread = new Thread(this::receiver, String.format("UART $%04X receiver", baseAddress));
    recvThread.start();
  }

  /**
   * Shut down the UART.  Stops the transmitter and receiver threads.
   */
  public void close() {
    closing = true;
    xmitThread.interrupt();
    recvThread.interrupt();
  }

  /**
   * Transmitter thread routine.
   *
   * This method loops until the UART is closed.
   */
  private void transmitter() {
    while(!closing) {
      synchronized(xmitFifo) {
        // Block if there's nothing to write.
        if(xmitHead == xmitTail) {
          // Set the Transmitter Empty and
          // Transmitter Holding Register Empty Flag
          LSR |= (TEMT | THRE);

          // If the THRE Interrupt is enabled, raise the interrupt.
          updateInterruptStatus();

          try {
            xmitFifo.wait();
          }
          catch (InterruptedException ie) {
            // eat it
          }
        }
      }
      // Sleep outside the lock.
      try {
        Thread.sleep(delayMillis, delayNanos);
      }
      catch (InterruptedException ie) {
        // eat it.
      }

      synchronized(xmitFifo) {
        if(xmitHead != xmitTail) {
          try {
            out.write(xmitFifo[xmitTail++]);
            out.flush();
          }
          catch (IOException ioe) {
            // eat it.
          }
          if(xmitTail == xmitFifo.length) {
            xmitTail = 0;
          }
        }
        if(xmitHead == xmitTail) {
          // Set the Transmitter Holding Register Empty flag.
          LSR |= THRE;
        }
      }
    }
  }

  /**
   * Receiver thread routine.
   *
   * This method loops until the UART is closed.
   */
  private void receiver() {
    while(!closing) {
      try {
        int c = in.read();
        synchronized(recvFifo) {
          if(((recvHead + 1) % recvFifo.length) == recvTail) {
            // TODO: handle overflow
          }
          else {
            recvFifo[recvHead++] = (byte)c;
            if(recvHead == recvFifo.length) {
              recvHead = 0;
            }
            // Set Data Ready flag.
            LSR |= DR;
          }
        }
      }
      catch (IOException ioe) {
        // eat it for now.
      }
    }
  }

  /**
   * Handle a read or write upon a positive clock tick, if the address bus
   * contains an address for a UART register.
   *
   * @param eventType the type of Signal event.
   */
  private void tick(Signal.EventType eventType) {
    // Return immediately if this is not a positive edge, or if the address
    // bus value is not for this module
    if(eventType != Signal.EventType.POSITIVE_EDGE ||
      ((short)(address.value() & addressMask) != baseAddress)) {
      return;
    }

    boolean  rwb = backplane.rwb().value();
    int reg = address.value() & 0x0007;
    if(rwb) {
      switch(reg) {
        case 0:
          // If the Divisor Latch Access Bit is zero, read the received value
          if((LCR & 0x80) == 0) {
            recv();
            data.value(RBR);
          }
          else {
            // otherwise, read the low Divisor Latch bits
            data.value(DLL);
          }
          break;
        case 1: data.value((LCR & 0x80) == 0 ? IER : DLM); break;
        case 2: data.value(IIR); break;
        case 3: data.value(LCR); break;
        case 4: data.value(MCR); break;
        case 5: data.value(LSR); break;
        case 6: data.value(MSR); break;
        case 7: data.value(SCR); break;
        default:
      }
    }
    else {
      byte b = (byte)data.value();
      switch(reg) {
        case 0:
          if((LCR & 0x80) == 0) {
            THR = b; xmit();
          }
          else {
            DLL = b;
            updateDivisor();
          }
          break;
        case 1:
          if((LCR & 0x80) == 0) {
            IER = b;
            updateInterruptStatus();
          }
          else {
            DLM = b;
            updateDivisor();
          }
          break;
        case 2: FCR = b; break;
        case 3: LCR = b; break;
        case 4: MCR = b; break;
        case 5: LSR = b; break;
        case 6: MSR = b; break;
        case 7: SCR = b; break;
        default:
      }
    }
  }

  /**
   * Update the interrupt registers to indicate the source of the interrupt.
   */
  private void updateInterruptStatus() {
    if(IER == 0) {
      IIR |= 1; IIR &= ~0b00001110;
      backplane.irqb().value(this, true);
      return;
    }

    if(((IER & 0b00000010) != 0) && ((LSR & THRE) != 0)) {
      IIR = 2;
      backplane.irqb().value(this, false);
    }
    else {
      IIR = 1;
    }
  }

  /**
   * Recalculate the delay time when the divisor bits are updated.
   */
  private void updateDivisor() {
    int bits = 5 + (LCR & 0x03) +        // data bits
               1 +                       // start bit
               1 + ((LCR & 0x04) >> 2) + // stop bits
                   ((LCR & 0x08) >> 3);  // parity bit
    double delay = (double)bits * (((DLM << 8) | (DLL & 0xFF)) & 0xFFFF) * 16 / frequency;
    delayNanos  = Double.valueOf(Math.floor(delay * 1_000_000_000d)).intValue();
    delayMillis = Double.valueOf(Math.floor(delayNanos / 1_000_000d)).intValue();
    delayNanos = delayNanos % 1_000_000;
  }

  /**
   * Write a byte to the transmitter FIFO.
   */
  private void xmit() {
    // Clear Transmitter Holding Register Empty flag.
    LSR &= ~THRE;
    synchronized(xmitFifo) {
      if(((xmitHead + 1) % xmitFifo.length) == xmitTail) {
        System.out.format("xmit: Overflow.  THR = %02X%n", THR);
        // TODO: handle overflow
      }
      else {
        xmitFifo[xmitHead++] = THR;
        if(xmitHead == xmitFifo.length) {
          xmitHead = 0;
        }
        // Clear the Transmitter Empty flag.
        LSR &= ~TEMT;

        // If the THRE Interrupt is enabled, clear the interrupt.
        updateInterruptStatus();
      }
      xmitFifo.notifyAll();
    }
  }

  /**
   * Read a byte from the receiver FIFO.
   */
  private void recv() {
    // populate RBR from fifo
    synchronized(recvFifo) {
      if(recvHead != recvTail) {
        RBR = recvFifo[recvTail++];
        if(recvTail == recvFifo.length)
          recvTail = 0;
      }
      if(recvHead == recvTail) {
        // Clear Data Ready flag.
        LSR &= ~DR;
      }
    }
  }
}
