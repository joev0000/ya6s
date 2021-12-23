package joev.ya6s;

/**
 * An enumeration of the source of values that can be placed on the
 * data bus during a cycle.
 */
public enum Data {
  OP,      // The instruction opcodeT
  DATA,    // Data for a register
  P,       // The Processor Status
  DO,      // The Zero Page address
  AAL,     // The Absolute Address, low byte
  AAH,     // The Absolute Address, high byte
  IO,      // Internal Operation, ignore the value
  PCL,     // The Program Counter, low byte
  PCH,     // The Program Counter, high byte
  NEW_PCL, // The new Program Counter, low byte
  NEW_PCH, // The new Program Coutner, high byte
  NULL     // Upon read, ignore the value
}
