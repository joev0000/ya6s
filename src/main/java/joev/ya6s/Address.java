package joev.ya6s;

/**
 * An enumeration of the source of values that can be placed
 * on the address bus during a cycle.
 */
public enum Address {
  PC,       // The Program Counter
  PC_INC,   // The postincremented Program Counter
  AA,       // The Absolute Address
  AA_INC,   // The postincremented Absolute Address
  AA_X,     // The Absolute Address plus X
  AA_X_1,   // The Absolute Address plus X plus 1
  AA_Y,     // The Absolute Address plus Y
  S,        // The Stack Pointer
  S_DEC,    // The postdecremented Stack Pointer
  S_INC,    // The preincremented Stack Pointer
  ZP,       // The Zero Page address
  ZP_INC,   // The postincremented Zero Page address
  ZP_X,     // The Zero Page address plus X
  ZP_X_1,   // The Zero Page address plus X plus 1
  ZP_X_INC, // The postincremented Zero Page address plus X
  ZP_Y,     // The Zero Page address plus Y
  VAL,      // The Vector Address, low byte
  VAH       // The Vector Address, high byte
}


