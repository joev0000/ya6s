/* Copyright (C) 2021, 2022 Joseph Vigneau */

package joev.ya6s.monitor;

import java.util.Objects;

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

  /**
   * Compare this DissassembleCommand with another Object.
   *
   * @param other the other Object to compare
   * @return true if the other Object is a DissassembleCommand with the same value.
   */
  @Override
  public boolean equals(Object other) {
    if(other instanceof DisassembleCommand o) {
      return this.address == o.address && this.count == o.count;
    }
    return false;
  }

  /**
   * Return the hash code of this DissassembleCommand.
   *
   * @return the hash code of this DissassembleCommand.
   */
  @Override
  public int hashCode() {
    return Objects.hash(address, count);
  }
}
