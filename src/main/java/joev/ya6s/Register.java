package joev.ya6s;

/**
 * An enumeration of the registers that can appear in the address or data bus of a cycle.
 */
public enum Register {
    A, X, Y, S, P, PC,
    OP, AAL, AAH, PC_INC, DATA, NULL, AA, IO, REG, DO, DO_INC, DO_X, DO_X_INC, DO_X_1, DO_Y,
    AA_X, AA_X_1, AA_INC,
    PCH, PCL, NEW_PCL, NEW_PCH, VAL, VAH,
    S_DEC, S_INC,
    AA_Y
  }

