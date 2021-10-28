package joev.ya6s;

/**
 * An enumeration of the machine addressing modes, which include lists of Cycles to execute.
 */
public enum AddressingMode {
    ILLEGAL("Illegal", ""),
    NOT_IMPLEMENTED("Not Implemented", ""),
    IMMEDIATE("Immediate", "#$02X",
      new Cycle(true, true, true,  Register.PC_INC, Register.OP,   true),
      new Cycle(true, true, false, Register.PC_INC, Register.DATA, true)
    ),
    ABSOLUTE("Absolute", "$04X",
      new Cycle(true, true, true,  Register.PC_INC, Register.OP,   true),
      new Cycle(true, true, false, Register.PC_INC, Register.AAL,  true),
      new Cycle(true, true, false, Register.PC_INC, Register.AAH,  true),
      new Cycle(true, true, false, Register.AA,     Register.DATA, true)
    ),
    // STA, STX, STY, STZ
    ABSOLUTE_W("Absolute", "$04X",
      new Cycle(true, true, true,  Register.PC_INC, Register.OP,   true),
      new Cycle(true, true, false, Register.PC_INC, Register.AAL,  true),
      new Cycle(true, true, false, Register.PC_INC, Register.AAH,  true),
      new Cycle(true, true, false, Register.AA,     Register.DATA, false)
    ),
    ABSOLUTE_RMW("Absolute", "$04X",
      new Cycle(true, true,  true,  Register.PC_INC, Register.OP,   true),
      new Cycle(true, true,  false, Register.PC_INC, Register.AAL,  true),
      new Cycle(true, true,  false, Register.PC_INC, Register.AAH,  true),
      new Cycle(true, false, false, Register.AA,     Register.DATA, true),
      new Cycle(true, false, false, Register.AA,     Register.IO,   true),
      new Cycle(true, false, false, Register.AA,     Register.DATA, false)
    ),
    ABSOLUTE_JMP("Absolute", "$04X",
      new Cycle(true, true,  true,   Register.PC_INC, Register.OP,   true),
      new Cycle(true, true,  false,  Register.PC_INC, Register.NEW_PCL, true),
      new Cycle(true, true,  false,  Register.PC_INC, Register.NEW_PCH, true)
    ),
    ABSOLUTE_JSR("Absolute", "$04X",
      new Cycle(true, true,  true,   Register.PC_INC, Register.OP,   true),
      new Cycle(true, true,  false,  Register.PC_INC, Register.NEW_PCL, true),
      new Cycle(true, true,  false,  Register.S,      Register.IO, true),
      new Cycle(true, true,  false,  Register.S_DEC,  Register.PCH, false),
      new Cycle(true, true,  false,  Register.S_DEC,  Register.PCL, false),
      new Cycle(true, true,  false,  Register.PC_INC, Register.NEW_PCH, true)
    ),
    ZERO_PAGE("Zero Page", "$02X",
      new Cycle(true, true,  true,   Register.PC_INC, Register.OP,   true),
      new Cycle(true, true,  false,  Register.PC_INC, Register.DO ,  true),
      new Cycle(true, true,  false,  Register.DO,     Register.DATA, true)
    ),
    // STA, STX, STY, STZ
    ZERO_PAGE_W("Zero Page", "$02X",
      new Cycle(true, true, true,  Register.PC_INC, Register.OP,   true),
      new Cycle(true, true, false, Register.PC_INC, Register.DO ,  true),
      new Cycle(true, true, false, Register.DO,     Register.DATA, false)
    ),
    // cycle[4] should be DO+1 for non-SMB, non-RMB.
    ZERO_PAGE_RMW("Zero Page", "$02X",
      new Cycle(true, true,  true,  Register.PC_INC, Register.OP,   true),
      new Cycle(true, true,  false, Register.PC_INC, Register.DO ,  true),
      new Cycle(true, false, false, Register.DO,     Register.DATA, true),
      new Cycle(true, false, false, Register.DO,     Register.IO,   true),
      new Cycle(true, false, false, Register.DO,     Register.DATA, false)
    ),
    ACCUMULATOR("Accumulator", "",
      new Cycle(true, true, true,  Register.PC_INC, Register.OP,   true),
      new Cycle(true, true, false, Register.PC,     Register.IO,   true)
    ),
    IMPLIED("Implied", "",
      new Cycle(true, true, true,  Register.PC_INC, Register.OP,   true),
      new Cycle(true, true, false, Register.PC,     Register.IO,   true)
    ),
    // Maybe one for WAI here?
    //,
    ZERO_PAGE_INDEXED("Zero Page Indexed", "($%02X),Y",
      new Cycle(true, true, true,  Register.PC_INC, Register.OP,   true),
      new Cycle(true, true, false, Register.PC_INC, Register.DO,  true),
      new Cycle(true, true, false, Register.DO_INC, Register.AAL,  true),
      new Cycle(true, true, false, Register.DO,     Register.AAH,  true),
      new Cycle(true, true, false, Register.AA_Y,   Register.DATA, true)
    ),
    ZERO_PAGE_INDEXED_W("Zero Page Indexed", "($%02X),Y",
      new Cycle(true, true, true,  Register.PC_INC, Register.OP,   true),
      new Cycle(true, true, false, Register.PC_INC, Register.DO,  true),
      new Cycle(true, true, false, Register.DO_INC, Register.AAL,  true),
      new Cycle(true, true, false, Register.DO,     Register.AAH,  true),
      new Cycle(true, true, false, Register.AA_Y,   Register.DATA, false)
    ),
    ZERO_PAGE_INDIRECT_X("Zero Page Indirect", "($%02X,X)",
      new Cycle(true, true, true,  Register.PC_INC, Register.OP,   true),
      new Cycle(true, true, false, Register.PC_INC, Register.DO,   true),
      new Cycle(true, true, false, Register.PC,     Register.IO,   true),
      new Cycle(true, true, false, Register.DO_X_INC, Register.AAL, true),
      new Cycle(true, true, false, Register.DO_X,    Register.AAH, true),
      new Cycle(true, true, false, Register.AA,     Register.DATA, true)
    ),
    ZERO_PAGE_INDIRECT_X_W("Zero Page Indirect", "($%02X,X)",
      new Cycle(true, true, true,  Register.PC_INC, Register.OP,   true),
      new Cycle(true, true, false, Register.PC_INC, Register.DO,   true),
      new Cycle(true, true, false, Register.PC,     Register.IO,   true),
      new Cycle(true, true, false, Register.DO_X_INC, Register.AAL, true),
      new Cycle(true, true, false, Register.DO_X,    Register.AAH, true),
      new Cycle(true, true, false, Register.AA,     Register.DATA, false)
    ),
    ZERO_PAGE_X("Zero Page, X", "$02X,X",
      new Cycle(true, true, true,  Register.PC_INC, Register.OP,   true),
      new Cycle(true, true,  false,  Register.PC_INC, Register.DO ,  true),
      new Cycle(true, true,  false,  Register.PC,     Register.IO, true),
      new Cycle(true, true, false, Register.DO_X, Register.DATA, true)
    ),
    ZERO_PAGE_X_W("Zero Page, X", "$02X,X",
      new Cycle(true, true, true,  Register.PC_INC, Register.OP,   true),
      new Cycle(true, true,  false,  Register.PC_INC, Register.DO ,  true),
      new Cycle(true, true,  false,  Register.PC,     Register.IO, true),
      new Cycle(true, true, false, Register.DO_X, Register.DATA, false)
    ),
    ZERO_PAGE_X_RMW("Zero Page, X", "$02X,X",
      new Cycle(true, true,  true,  Register.PC_INC, Register.OP,   true),
      new Cycle(true, true,  false, Register.PC_INC, Register.DO ,  true),
      new Cycle(true, true,  false, Register.PC,     Register.NULL,  true), // was IO
      new Cycle(true, false, false, Register.DO_X,   Register.DATA, true),
      new Cycle(true, false, false, Register.DO_X  , Register.IO,   true), // should be X+1
      new Cycle(true, false, false, Register.DO_X,   Register.DATA, false)
    ),
    ZERO_PAGE_Y("Zero Page, Y", "$02X,Y",
      new Cycle(true, true, true,  Register.PC_INC, Register.OP,   true),
      new Cycle(true, true,  false,  Register.PC_INC, Register.DO ,  true),
      new Cycle(true, true,  false,  Register.PC,     Register.IO, true),
      new Cycle(true, true, false, Register.DO_Y, Register.DATA, true)
    ),
    ZERO_PAGE_Y_W("Zero Page, Y", "$02X,Y",
      new Cycle(true, true, true,  Register.PC_INC, Register.OP,   true),
      new Cycle(true, true,  false,  Register.PC_INC, Register.DO ,  true),
      new Cycle(true, true,  false,  Register.PC,     Register.IO, true),
      new Cycle(true, true, false, Register.DO_Y, Register.DATA, false)
    ),
    ABSOLUTE_X("Absolute, X", "$04X,X",
      new Cycle(true, true, true,  Register.PC_INC, Register.OP,   true),
      new Cycle(true, true, false, Register.PC_INC, Register.AAL,  true),
      new Cycle(true, true, false, Register.PC_INC, Register.AAH,  true),
      new Cycle(true, true, false, Register.AA_X,   Register.DATA, true)
    ),
    ABSOLUTE_X_W("Absolute, X", "$04X,X",
      new Cycle(true, true, true,  Register.PC_INC, Register.OP,   true),
      new Cycle(true, true, false, Register.PC_INC, Register.AAL,  true),
      new Cycle(true, true, false, Register.PC_INC, Register.AAH,  true),
      new Cycle(true, true, false, Register.PC,     Register.IO,   true),
      new Cycle(true, true, false, Register.AA_X,   Register.DATA, false)
    ),
    ABSOLUTE_X_RMW("Absolute, X", "$04X,X",
      new Cycle(true, true,  true,  Register.PC_INC, Register.OP,   true),
      new Cycle(true, true,  false, Register.PC_INC, Register.AAL,  true),
      new Cycle(true, true,  false, Register.PC_INC, Register.AAH,  true),
      new Cycle(true, true,  false, Register.AA_X,   Register.NULL, true),
      new Cycle(true, false, false, Register.AA_X,   Register.DATA, true),
      new Cycle(true, false, false, Register.AA_X  , Register.IO,   true), // Should read X+1
      new Cycle(true, false, false, Register.AA_X,   Register.DATA, false)
    ),
    ABSOLUTE_Y("Absolute, Y", "$04X,Y",
      new Cycle(true, true, true,  Register.PC_INC, Register.OP,   true),
      new Cycle(true, true, false, Register.PC_INC, Register.AAL,  true),
      new Cycle(true, true, false, Register.PC_INC, Register.AAH,  true),
      new Cycle(true, true, false, Register.AA_Y,   Register.DATA, true)
    ),
    ABSOLUTE_Y_W("Absolute, Y", "$04X,Y",
      new Cycle(true, true, true,  Register.PC_INC, Register.OP,   true),
      new Cycle(true, true, false, Register.PC_INC, Register.AAL,  true),
      new Cycle(true, true, false, Register.PC_INC, Register.AAH,  true),
      new Cycle(true, true, false, Register.AA_Y,   Register.DATA, false)
    ),
    RELATIVE("Relative", "$02X",
      new Cycle(true, true, true,  Register.PC_INC, Register.OP,   true),
      new Cycle(true, true, false, Register.PC_INC, Register.DATA, true)
    ),
    RELATIVE_BB("Relative", "$02X",
      new Cycle(true, true, true,  Register.PC_INC, Register.OP,   true),
      new Cycle(true, true, false, Register.PC_INC, Register.DO,   true),
      new Cycle(true, true, false, Register.DO,     Register.DATA, true),
      new Cycle(true, true, false, Register.DO,     Register.IO,   true),
      new Cycle(true, true, false, Register.PC_INC, Register.DATA, true) // this is not what the spec does.
    ),
    ABSOLUTE_INDIRECT("Absolute Indirect", "%04X",
      new Cycle(true, true, true,  Register.PC_INC, Register.OP,  true),
      new Cycle(true, true, false, Register.PC_INC, Register.AAL, true),
      new Cycle(true, true, false, Register.PC    , Register.AAH, true),
      new Cycle(true, true, false, Register.PC,     Register.AAH, true),
      new Cycle(true, true, false, Register.AA_INC, Register.NEW_PCL, true),
      new Cycle(true, true, false, Register.AA    , Register.NEW_PCH, true)
    ),
    STACK_INTERRUPT("Stack", "",
      new Cycle(true, true, true,  Register.PC_INC, Register.OP,   true),
      new Cycle(true, true, false, Register.PC,     Register.IO,   true),
      new Cycle(true, true, true,  Register.S_DEC,  Register.PCH,  true),
      new Cycle(true, true, true,  Register.S_DEC,  Register.PCL,  true),
      new Cycle(true, true, true,  Register.S,      Register.P,  true),
      new Cycle(false,true, true,  Register.VAL,    Register.NEW_PCL, true),
      new Cycle(false,true, true,  Register.VAH,    Register.NEW_PCH, true)
    ),
    STACK_BRK("Stack", "",
      new Cycle(true, true, true,  Register.PC_INC, Register.OP,   true),
      new Cycle(true, true, false, Register.PC_INC, Register.IO, true),
      new Cycle(true, true,  false,  Register.S_DEC,  Register.PCH, false),
      new Cycle(true, true,  false,  Register.S_DEC,  Register.PCL, false),
     // new Cycle(true, true,  false,  Register.S_DEC,  Register.P, false),
      new Cycle(true, true,  false,  Register.S_DEC,  Register.DATA, false),
      new Cycle(false,true, true,  Register.VAL,    Register.NEW_PCL, true),
      new Cycle(false,true, true,  Register.VAH,    Register.NEW_PCH, true)
    ),
    STACK_RTI("Stack", "",
      new Cycle(true, true, true,  Register.PC_INC, Register.OP,   true),
      new Cycle(true, true, false, Register.PC, Register.IO, true),
      new Cycle(true, true, false, Register.S,     Register.IO, true),
      new Cycle(true, true, false, Register.S_INC, Register.P, true),
      new Cycle(true, true, false, Register.S_INC, Register.NEW_PCL, true),
      new Cycle(true, true, false, Register.S_INC, Register.NEW_PCH, true)
    ),
    STACK_RTS("Stack", "",
      new Cycle(true, true, true,  Register.PC_INC, Register.OP,   true),
      new Cycle(true, true, false, Register.PC,     Register.IO,   true),
      new Cycle(true, true, false, Register.S,      Register.IO,   true),
      new Cycle(true, true, false, Register.S_INC,  Register.NEW_PCL,  true),
      new Cycle(true, true, false, Register.S_INC,  Register.NEW_PCH,  true),
      new Cycle(true, true, false, Register.PC,     Register.IO,   true)
    ),
    STACK_PUSH("Stack", "",
      new Cycle(true, true, true,  Register.PC_INC, Register.OP,   true),
      new Cycle(true, true, false, Register.PC,     Register.IO,   true),
      new Cycle(true, true, false, Register.S_DEC,  Register.DATA, false)
      ),
    STACK_PULL("Stack", "",
      new Cycle(true, true, true,  Register.PC_INC, Register.OP,   true),
      new Cycle(true, true, false, Register.PC,     Register.IO,   true),
      new Cycle(true, true, false, Register.PC,     Register.IO,   true),
      new Cycle(true, true, false, Register.S_INC,  Register.DATA, true)
    ),
    ABSOLUTE_INDEXED_INDIRECT("Absolute Indexed Indirect", "%04X",
      new Cycle(true, true, true,  Register.PC_INC, Register.OP,   true),
      new Cycle(true, true, false, Register.PC_INC, Register.AAL, true),
      new Cycle(true, true, false, Register.PC    , Register.AAH, true),
      new Cycle(true, true, false, Register.PC,     Register.IO, true),
      new Cycle(true, true, false, Register.AA_X,   Register.NEW_PCL, true),
      new Cycle(true, true, false, Register.AA_X_1, Register.NEW_PCH, true)
    ),
    ZERO_PAGE_INDIRECT("Zero Page Indirect", "($%02X)",
      new Cycle(true, true, true,  Register.PC_INC, Register.OP,   true),
      new Cycle(true, true, false, Register.PC_INC, Register.DO ,  true),
      new Cycle(true, true, false, Register.DO_INC, Register.AAL, true),
      new Cycle(true, true, false, Register.DO_INC, Register.AAH, true),
      new Cycle(true, true, false, Register.AA,     Register.DATA, true)
    ),
    ZERO_PAGE_INDIRECT_W("Zero Page Indirect", "($%02X)",
      new Cycle(true, true, true,  Register.PC_INC, Register.OP,   true),
      new Cycle(true, true, false, Register.PC_INC, Register.DO ,  true),
      new Cycle(true, true, false, Register.DO_INC, Register.AAL, true),
      new Cycle(true, true, false, Register.DO_INC, Register.AAH, true),
      new Cycle(true, true, false, Register.AA,     Register.DATA, false)
    ),
    NOP_1BYTE_1CYCLE("NOP", "",
      new Cycle(true, true, true,  Register.PC_INC, Register.OP,   true),
      new Cycle(true, true, false, Register.PC    , Register.NULL, true) // Bug: should be one cycle.
    ),
    NOP_2BYTE_2CYCLE("NOP", "",
      new Cycle(true, true, true,  Register.PC_INC, Register.OP,   true),
      new Cycle(true, true, false, Register.PC_INC, Register.NULL, true)
    ),
    NOP_2BYTE_3CYCLE("NOP", "",
      new Cycle(true, true, true,  Register.PC_INC, Register.OP,   true),
      new Cycle(true, true, false, Register.PC_INC, Register.NULL, true),
      new Cycle(true, true, false, Register.PC    , Register.NULL, true)
    ),
    NOP_2BYTE_4CYCLE("NOP", "",
      new Cycle(true, true, true,  Register.PC_INC, Register.OP,   true),
      new Cycle(true, true, false, Register.PC_INC, Register.NULL, true),
      new Cycle(true, true, false, Register.PC    , Register.NULL, true),
      new Cycle(true, true, false, Register.PC    , Register.NULL, true)
    ),
    NOP_3BYTE_4CYCLE("NOP", "",
      new Cycle(true, true, true,  Register.PC_INC, Register.OP,   true),
      new Cycle(true, true, false, Register.PC_INC, Register.NULL, true),
      new Cycle(true, true, false, Register.PC_INC, Register.NULL, true),
      new Cycle(true, true, false, Register.PC    , Register.NULL, true)
    ),
    NOP_3BYTE_8CYCLE("NOP", "",
      new Cycle(true, true, true,  Register.PC_INC, Register.OP,   true),
      new Cycle(true, true, false, Register.PC_INC, Register.NULL, true),
      new Cycle(true, true, false, Register.PC_INC, Register.NULL, true),
      new Cycle(true, true, false, Register.PC    , Register.NULL, true),
      new Cycle(true, true, false, Register.PC    , Register.NULL, true),
      new Cycle(true, true, false, Register.PC    , Register.NULL, true),
      new Cycle(true, true, false, Register.PC    , Register.NULL, true),
      new Cycle(true, true, false, Register.PC    , Register.NULL, true)
    );

    private final String modeName;
    private final String format;
    private final Cycle[] cycles;
    AddressingMode(String modeName, String format, Cycle... cycles) {
      this.modeName = modeName;
      this.format = format;
      this.cycles = cycles;
    }
    public String modeName() { return modeName; }
    public String format()   { return format; }
    public Cycle[] cycles()  { return cycles; }
  }

