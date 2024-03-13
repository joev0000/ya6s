/* Copyright (C) 2021, 2022 Joseph Vigneau */

package joev.ya6s;

import joev.ya6s.signals.Signal;

/**
 * The Clock is responsible for cycling a Signal between the true and false
 * states.
 */
public final class Clock {
  private final Signal clock;
  private Thread thread = null;

  private volatile boolean running = false;
  private long delayMillis;
  private int  delayNanos;

  /**
   * Create a Clock for the Signal with the fastest possible frequency.
   *
   * @param clock the Signal to cycle.
   */
  public Clock(Signal clock) {
    this(clock, Double.MAX_VALUE);
  }

  /**
   * Create a Clock for the given Signal at the given frequency.
   *
   * @param clock the signal to cycle.
   * @param frequency the maximum frequency in hertz.
   */
  public Clock(Signal clock, double frequency) {
    frequency *= 2;
    frequency(frequency);
    this.clock = clock;
  }

  /**
   * Set the frequency of the Clock.  Applied the next time the clock
   * is started.
   *
   * @param frequency the maximum frequency in hertz.
   */
  public void frequency(double frequency) {
    delayMillis = (long)Math.floor(1000d/frequency);
    delayNanos =  (int)((1000000000d/frequency) - (delayMillis * 1000000d));
  }

  /**
   * Get the frequency of the Clock the next time the Clock is started.
   * May not be exactly equal to the frequency that was set due to
   * rounding precision.
   *
   * @return the frequency of the Clock
   */
  public double frequency() {
    double denominator = (delayMillis / 1000d) + (delayNanos / 1000000000d);
    return (denominator == 0d ? Double.MAX_VALUE : 1 / denominator) / 2d;
  }

  /**
   * Return true if the Clock is running.
   *
   * @return true if the Clock is running.
   */
  public boolean running() {
    return running;
  }

  /**
   * Start the clock.  Creates a new thread and switches the Signal until
   * the Clock is stopped.  The clock is delayed based on the frequency.
   * If the frequency is Double.MAX_VALUE, the clock will run as fast
   * as possible.
   *
   * This method does nothing if the clock is already running.
   */
  public void start() {
    if(!running) {
      Runnable runnable = (delayMillis == 0 && delayNanos == 0) ?
        () -> {
          clock.value(true);
          while(running) {
            clock.value(false);
            clock.value(true);
          }
       } :
       () -> {
          try {
            clock.value(true);
            while(running) {
              Thread.sleep(delayMillis, delayNanos);
              clock.value(false);
              Thread.sleep(delayMillis, delayNanos);
              clock.value(true);
            }
          }
          catch (InterruptedException ie) {
            running = false;
          }
        };
      running = true;

      thread = new Thread(runnable, "Clock");
      thread.start();
    }
  }

  /**
   * Stop the clock.
   */
  public void stop() {
    if(running) {
      running = false;
      if(thread != null && thread != Thread.currentThread()) {
        try {
          thread.join();
          thread = null;
        }
        catch (InterruptedException ie) {
        }
      }
    }
  }

  /**
   * Cycle the clock once.  Does nothing if the Clock is running.
   */
  public void cycle() {
    if(!running) {
      clock.value(false);
      clock.value(true);
    }
  }
}
