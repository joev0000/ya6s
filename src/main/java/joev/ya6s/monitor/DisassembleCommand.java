package joev.ya6s.monitor;

/**
 * Monitor command to provide disassembly of part of the program.
 */
public class DisassembleCommand implements Command {
  private short address;
  private int count;

  /**
   * Create a Disassemble command that starts from the given address
   * for the given count of instructions.
   *
   * @param address the address to start from, -1 for the current address.
   * @param count the number of instructions to disassemble
   */
  public DisassembleCommand(short address, int count) {
    this.address = address;
    this.count = count;
  }

  /**
   * Call upon the monitor to disassemble the instructions.
   *
   * @param monitor the Monitor to use for disassembly.
   * @return null
   */
  @Override
  public Command execute(Monitor monitor) {
    System.out.println(monitor.disassemble(address, count));
    return null;
  }
}
