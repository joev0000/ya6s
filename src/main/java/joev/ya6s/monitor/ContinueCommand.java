package joev.ya6s.monitor;

import joev.ya6s.Backplane;
import joev.ya6s.W65C02;
import joev.ya6s.signals.Signal;

/**
 * The Continue command.
 */
public class ContinueCommand implements Command {
  private static final ContinueCommand instance = new ContinueCommand();

  /**
   * Private constructor so this cannot be instantiated.
   */
  private ContinueCommand() { }

  /**
   * Return the static instance of this command.
   *
   * @return the continue Command.
   */
  public static ContinueCommand instance() {
    return instance;
  }

  /**
   * Execute the command; run until the CPU is stopped.
   *
   * @param backplane the Backplane of the system to run
   * @param cpu the CPU of the system to run.
   * @return the next suggested Command, or null.
   */
  @Override
  public Command execute(Backplane backplane, W65C02 cpu) {
    Signal clock = backplane.clock();
    short oldPC = 0;
    while(!cpu.stopped()) { // or hit a breakpoint
      if(backplane.sync().value()) {
        if(cpu.pc() == oldPC) {
          System.out.println("Loop detected.");
          break;
        }
        oldPC = cpu.pc();
      }
      clock.value(true);
      clock.value(false);
    }
    if(cpu.stopped()) {
      System.out.println("Stopped.");
    }
    System.out.format("PC: $%04X  A: $%02X  X: $%02X  Y: $%02X  S: $%02X  P: $%02X (%s)%n", cpu.pc(), cpu.a(), cpu.x(), cpu.y(), cpu.s(), cpu.p(), cpu.status());
    return null;
  }

  /**
   * Get a human-readable representation of this command.
   *
   * @return "cont"
   */
  @Override
  public String toString() {
    return "cont";
  }
}
