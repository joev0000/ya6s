package joev.ya6s;

import static joev.ya6s.Register.*;

/**
 * An enumeration of the machine addressing modes, which include lists of Cycles to execute.
 */
public enum AddressingMode {
    ILLEGAL("Illegal", ""),
    NOT_IMPLEMENTED("Not Implemented", ""),
    IMMEDIATE("Immediate", "#$02X",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   DATA,    true)
    ),
    ABSOLUTE("Absolute", "$04X",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   AAL,     true),
      new Cycle(true,  true,  false, PC_INC,   AAH,     true),
      new Cycle(true,  true,  false, AA,       DATA,    true)
    ),
    // STA, STX, STY, STZ
    ABSOLUTE_W("Absolute", "$04X",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   AAL,     true),
      new Cycle(true,  true,  false, PC_INC,   AAH,     true),
      new Cycle(true,  true,  false, AA,       DATA,    false)
    ),
    ABSOLUTE_RMW("Absolute", "$04X",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   AAL,     true),
      new Cycle(true,  true,  false, PC_INC,   AAH,     true),
      new Cycle(true,  false, false, AA,       DATA,    true),
      new Cycle(true,  false, false, AA,       IO,      true),
      new Cycle(true,  false, false, AA,       DATA,    false)
    ),
    ABSOLUTE_JMP("Absolute", "$04X",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   NEW_PCL, true),
      new Cycle(true,  true,  false, PC_INC,   NEW_PCH, true)
    ),
    ABSOLUTE_JSR("Absolute", "$04X",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   NEW_PCL, true),
      new Cycle(true,  true,  false, S,        IO,      true),
      new Cycle(true,  true,  false, S_DEC,    PCH,     false),
      new Cycle(true,  true,  false, S_DEC,    PCL,     false),
      new Cycle(true,  true,  false, PC_INC,   NEW_PCH, true)
    ),
    ZERO_PAGE("Zero Page", "$02X",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   DO,      true),
      new Cycle(true,  true,  false, DO,       DATA,    true)
    ),
    // STA, STX, STY, STZ
    ZERO_PAGE_W("Zero Page", "$02X",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   DO ,     true),
      new Cycle(true,  true,  false, DO,       DATA,    false)
    ),
    // cycle[4] should be DO+1 for non-SMB, non-RMB.
    ZERO_PAGE_RMW("Zero Page", "$02X",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  false, false, PC_INC,   DO ,     true),
      new Cycle(true,  false, false, DO,       DATA,    true),
      new Cycle(true,  false, false, DO,       IO,      true),
      new Cycle(true,  false, false, DO,       DATA,    false)
    ),
    ACCUMULATOR("Accumulator", "",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC,       IO,      true)
    ),
    IMPLIED("Implied", "",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC,       IO,      true)
    ),
    // Maybe one for WAI here?
    //,
    ZERO_PAGE_INDEXED("Zero Page Indexed", "($%02X),Y",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   DO,      true),
      new Cycle(true,  true,  false, DO_INC,   AAL,     true),
      new Cycle(true,  true,  false, DO,       AAH,     true),
      new Cycle(true,  true,  false, AA_Y,     DATA,    true)
    ),
    ZERO_PAGE_INDEXED_W("Zero Page Indexed", "($%02X),Y",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   DO,      true),
      new Cycle(true,  true,  false, DO_INC,   AAL,     true),
      new Cycle(true,  true,  false, DO,       AAH,     true),
      new Cycle(true,  true,  false, AA_Y,     DATA,    false)
    ),
    ZERO_PAGE_INDIRECT_X("Zero Page Indirect", "($%02X,X)",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   DO,      true),
      new Cycle(true,  true,  false, PC,       IO,      true),
      new Cycle(true,  true,  false, DO_X_INC, AAL,     true),
      new Cycle(true,  true,  false, DO_X,     AAH,     true),
      new Cycle(true,  true,  false, AA,       DATA,    true)
    ),
    ZERO_PAGE_INDIRECT_X_W("Zero Page Indirect", "($%02X,X)",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   DO,      true),
      new Cycle(true,  true,  false, PC,       IO,      true),
      new Cycle(true,  true,  false, DO_X_INC, AAL,     true),
      new Cycle(true,  true,  false, DO_X,     AAH,     true),
      new Cycle(true,  true,  false, AA,       DATA,    false)
    ),
    ZERO_PAGE_X("Zero Page, X", "$02X,X",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   DO,      true),
      new Cycle(true,  true,  false, PC,       IO,      true),
      new Cycle(true,  true,  false, DO_X,     DATA,    true)
    ),
    ZERO_PAGE_X_W("Zero Page, X", "$02X,X",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   DO,      true),
      new Cycle(true,  true,  false, PC,       IO,      true),
      new Cycle(true,  true,  false, DO_X,     DATA,    false)
    ),
    ZERO_PAGE_X_RMW("Zero Page, X", "$02X,X",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   DO,      true),
      new Cycle(true,  true,  false, PC,       NULL,    true), // was IO
      new Cycle(true,  false, false, DO_X,     DATA,    true),
      new Cycle(true,  false, false, DO_X  ,   IO,      true), // should be X+1
      new Cycle(true,  false, false, DO_X,     DATA,    false)
    ),
    ZERO_PAGE_Y("Zero Page, Y", "$02X,Y",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   DO,      true),
      new Cycle(true,  true,  false, PC,       IO,      true),
      new Cycle(true,  true,  false, DO_Y,     DATA,    true)
    ),
    ZERO_PAGE_Y_W("Zero Page, Y", "$02X,Y",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   DO,      true),
      new Cycle(true,  true,  false, PC,       IO,      true),
      new Cycle(true,  true,  false, DO_Y,     DATA,    false)
    ),
    ABSOLUTE_X("Absolute, X", "$04X,X",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   AAL,     true),
      new Cycle(true,  true,  false, PC_INC,   AAH,     true),
      new Cycle(true,  true,  false, AA_X,     DATA,    true)
    ),
    ABSOLUTE_X_W("Absolute, X", "$04X,X",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   AAL,     true),
      new Cycle(true,  true,  false, PC_INC,   AAH,     true),
      new Cycle(true,  true,  false, PC,       IO,      true),
      new Cycle(true,  true,  false, AA_X,     DATA,    false)
    ),
    ABSOLUTE_X_RMW("Absolute, X", "$04X,X",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   AAL,     true),
      new Cycle(true,  true,  false, PC_INC,   AAH,     true),
      new Cycle(true,  true,  false, AA_X,     NULL,    true),
      new Cycle(true,  false, false, AA_X,     DATA,    true),
      new Cycle(true,  false, false, AA_X,     IO,      true), // Should read X+1
      new Cycle(true,  false, false, AA_X,     DATA,    false)
    ),
    ABSOLUTE_Y("Absolute, Y", "$04X,Y",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   AAL,     true),
      new Cycle(true,  true,  false, PC_INC,   AAH,     true),
      new Cycle(true,  true,  false, AA_Y,     DATA,    true)
    ),
    ABSOLUTE_Y_W("Absolute, Y", "$04X,Y",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   AAL,     true),
      new Cycle(true,  true,  false, PC_INC,   AAH,     true),
      new Cycle(true,  true,  false, AA_Y,     DATA,    false)
    ),
    RELATIVE("Relative", "$02X",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   DATA,    true)
    ),
    RELATIVE_BB("Relative", "$02X",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   DO,      true),
      new Cycle(true,  true,  false, DO,       DATA,    true),
      new Cycle(true,  true,  false, DO,       IO,      true),
      new Cycle(true,  true,  false, PC_INC,   DATA,    true) // this is not what the spec does.
    ),
    ABSOLUTE_INDIRECT("Absolute Indirect", "%04X",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   AAL,     true),
      new Cycle(true,  true,  false, PC,       AAH,     true),
      new Cycle(true,  true,  false, PC,       AAH,     true),
      new Cycle(true,  true,  false, AA_INC,   NEW_PCL, true),
      new Cycle(true,  true,  false, AA,       NEW_PCH, true)
    ),
    STACK_INTERRUPT("Stack", "",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC,       IO,      true),
      new Cycle(true,  true,  false, S_DEC,    PCH,     false),
      new Cycle(true,  true,  false, S_DEC,    PCL,     false),
      new Cycle(true,  true,  false, S_DEC,    P,       false),
      new Cycle(false, true,  false, VAL,      NEW_PCL, true),
      new Cycle(false, true,  false, VAH,      NEW_PCH, true)
    ),
    STACK_BRK("Stack", "",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   IO,      true),
      new Cycle(true,  true,  false, S_DEC,    PCH,     false),
      new Cycle(true,  true,  false, S_DEC,    PCL,     false),
      new Cycle(true,  true,  false, S_DEC,    DATA,    false),
      new Cycle(false, true,  false, VAL,      NEW_PCL, true),
      new Cycle(false, true,  false, VAH,      NEW_PCH, true)
    ),
    STACK_RTI("Stack", "",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC,       IO,      true),
      new Cycle(true,  true,  false, S,        IO,      true),
      new Cycle(true,  true,  false, S_INC,    P,       true),
      new Cycle(true,  true,  false, S_INC,    NEW_PCL, true),
      new Cycle(true,  true,  false, S_INC,    NEW_PCH, true)
    ),
    STACK_RTS("Stack", "",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC,       IO,      true),
      new Cycle(true,  true,  false, S,        IO,      true),
      new Cycle(true,  true,  false, S_INC,    NEW_PCL, true),
      new Cycle(true,  true,  false, S_INC,    NEW_PCH, true),
      new Cycle(true,  true,  false, PC,       IO,      true)
    ),
    STACK_PUSH("Stack", "",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC,       IO,      true),
      new Cycle(true,  true,  false, S_DEC,    DATA,    false)
      ),
    STACK_PULL("Stack", "",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC,       IO,      true),
      new Cycle(true,  true,  false, PC,       IO,      true),
      new Cycle(true,  true,  false, S_INC,    DATA,    true)
    ),
    ABSOLUTE_INDEXED_INDIRECT("Absolute Indexed Indirect", "%04X",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   AAL,     true),
      new Cycle(true,  true,  false, PC,       AAH,     true),
      new Cycle(true,  true,  false, PC,       IO,      true),
      new Cycle(true,  true,  false, AA_X,     NEW_PCL, true),
      new Cycle(true,  true,  false, AA_X_1,   NEW_PCH, true)
    ),
    ZERO_PAGE_INDIRECT("Zero Page Indirect", "($%02X)",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   DO,      true),
      new Cycle(true,  true,  false, DO_INC,   AAL,     true),
      new Cycle(true,  true,  false, DO_INC,   AAH,     true),
      new Cycle(true,  true,  false, AA,       DATA,    true)
    ),
    ZERO_PAGE_INDIRECT_W("Zero Page Indirect", "($%02X)",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   DO,      true),
      new Cycle(true,  true,  false, DO_INC,   AAL,     true),
      new Cycle(true,  true,  false, DO_INC,   AAH,     true),
      new Cycle(true,  true,  false, AA,       DATA,    false)
    ),
    NOP_1BYTE_1CYCLE("NOP", "",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC,       NULL,    true) // Bug: should be one cycle.
    ),
    NOP_2BYTE_2CYCLE("NOP", "",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   NULL,    true)
    ),
    NOP_2BYTE_3CYCLE("NOP", "",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   NULL,    true),
      new Cycle(true,  true,  false, PC,       NULL,    true)
    ),
    NOP_2BYTE_4CYCLE("NOP", "",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   NULL,    true),
      new Cycle(true,  true,  false, PC,       NULL,    true),
      new Cycle(true,  true,  false, PC,       NULL,    true)
    ),
    NOP_3BYTE_4CYCLE("NOP", "",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   NULL,    true),
      new Cycle(true,  true,  false, PC_INC,   NULL,    true),
      new Cycle(true,  true,  false, PC,       NULL,    true)
    ),
    NOP_3BYTE_8CYCLE("NOP", "",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   NULL,    true),
      new Cycle(true,  true,  false, PC_INC,   NULL,    true),
      new Cycle(true,  true,  false, PC,       NULL,    true),
      new Cycle(true,  true,  false, PC,       NULL,    true),
      new Cycle(true,  true,  false, PC,       NULL,    true),
      new Cycle(true,  true,  false, PC,       NULL,    true),
      new Cycle(true,  true,  false, PC,       NULL,    true)
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

