/* Copyright (C) 2021, 2022 Joseph Vigneau */

package joev.ya6s;

import static joev.ya6s.Address.*;
import static joev.ya6s.Data.*;

/**
 * An enumeration of the machine addressing modes, which include lists of Cycles to execute.
 */
public enum AddressingMode {
    ILLEGAL("Illegal", 1, ""),
    NOT_IMPLEMENTED("Not Implemented", 1, ""),
    IMMEDIATE("Immediate", 2, "#$%02X",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   DATA,    true)
    ),
    ABSOLUTE("Absolute", 3, "$%04X",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   AAL,     true),
      new Cycle(true,  true,  false, PC_INC,   AAH,     true),
      new Cycle(true,  true,  false, AA,       DATA,    true)
    ),
    // STA, STX, STY, STZ
    ABSOLUTE_W("Absolute", 3, "$%04X",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   AAL,     true),
      new Cycle(true,  true,  false, PC_INC,   AAH,     true),
      new Cycle(true,  true,  false, AA,       DATA,    false)
    ),
    ABSOLUTE_RMW("Absolute", 3, "$%04X",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   AAL,     true),
      new Cycle(true,  true,  false, PC_INC,   AAH,     true),
      new Cycle(true,  false, false, AA,       DATA,    true),
      new Cycle(true,  false, false, AA,       IO,      true),
      new Cycle(true,  false, false, AA,       DATA,    false)
    ),
    ABSOLUTE_JMP("Absolute", 3, "$%04X",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   NEW_PCL, true),
      new Cycle(true,  true,  false, PC_INC,   NEW_PCH, true)
    ),
    ABSOLUTE_JSR("Absolute", 3, "$%04X",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   NEW_PCL, true),
      new Cycle(true,  true,  false, S,        IO,      true),
      new Cycle(true,  true,  false, S_DEC,    PCH,     false),
      new Cycle(true,  true,  false, S_DEC,    PCL,     false),
      new Cycle(true,  true,  false, PC_INC,   NEW_PCH, true)
    ),
    ZERO_PAGE("Zero Page", 2, "$%02X",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   DO,      true),
      new Cycle(true,  true,  false, ZP,       DATA,    true)
    ),
    // STA, STX, STY, STZ
    ZERO_PAGE_W("Zero Page", 2, "$%02X",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   DO ,     true),
      new Cycle(true,  true,  false, ZP,       DATA,    false)
    ),
    // cycle[4] should be DO+1 for non-SMB, non-RMB.
    ZERO_PAGE_RMW("Zero Page", 2, "$%02X",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  false, false, PC_INC,   DO ,     true),
      new Cycle(true,  false, false, ZP,       DATA,    true),
      new Cycle(true,  false, false, ZP,       IO,      true),
      new Cycle(true,  false, false, ZP,       DATA,    false)
    ),
    ACCUMULATOR("Accumulator", 1, "",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC,       IO,      true)
    ),
    IMPLIED("Implied", 1, "",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC,       IO,      true)
    ),
    IMPLIED_WAI("Implied", 1, "",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC,       IO,      true),
      new Cycle(true,  true,  false, PC,       IO,      true)
    ),
    ZERO_PAGE_INDEXED("Zero Page Indirect Indexed", 2, "($%02X),Y",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   DO,      true),
      new Cycle(true,  true,  false, ZP_INC,   AAL,     true),
      new Cycle(true,  true,  false, ZP,       AAH,     true),
      new Cycle(true,  true,  false, AA_Y,     DATA,    true)
    ),
    ZERO_PAGE_INDEXED_W("Zero Page Indirect Indexed", 2, "($%02X),Y",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   DO,      true),
      new Cycle(true,  true,  false, ZP_INC,   AAL,     true),
      new Cycle(true,  true,  false, ZP,       AAH,     true),
      new Cycle(true,  true,  false, AA_Y,     DATA,    false)
    ),
    ZERO_PAGE_INDIRECT_X("Zero Page Indexed Indirect", 2, "($%02X,X)",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   DO,      true),
      new Cycle(true,  true,  false, PC,       IO,      true),
      new Cycle(true,  true,  false, ZP_X_INC, AAL,     true),
      new Cycle(true,  true,  false, ZP_X,     AAH,     true),
      new Cycle(true,  true,  false, AA,       DATA,    true)
    ),
    ZERO_PAGE_INDIRECT_X_W("Zero Page Indexed Indirect", 2, "($%02X,X)",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   DO,      true),
      new Cycle(true,  true,  false, PC,       IO,      true),
      new Cycle(true,  true,  false, ZP_X_INC, AAL,     true),
      new Cycle(true,  true,  false, ZP_X,     AAH,     true),
      new Cycle(true,  true,  false, AA,       DATA,    false)
    ),
    ZERO_PAGE_X("Zero Page, X", 2, "$%02X,X",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   DO,      true),
      new Cycle(true,  true,  false, PC,       IO,      true),
      new Cycle(true,  true,  false, ZP_X,     DATA,    true)
    ),
    ZERO_PAGE_X_W("Zero Page, X", 2, "$%02X,X",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   DO,      true),
      new Cycle(true,  true,  false, PC,       IO,      true),
      new Cycle(true,  true,  false, ZP_X,     DATA,    false)
    ),
    ZERO_PAGE_X_RMW("Zero Page, X", 2, "$%02X,X",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   DO,      true),
      new Cycle(true,  true,  false, PC,       NULL,    true), // was IO
      new Cycle(true,  false, false, ZP_X,     DATA,    true),
      new Cycle(true,  false, false, ZP_X  ,   IO,      true), // should be X+1
      new Cycle(true,  false, false, ZP_X,     DATA,    false)
    ),
    ZERO_PAGE_Y("Zero Page, Y", 2, "$%02X,Y",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   DO,      true),
      new Cycle(true,  true,  false, PC,       IO,      true),
      new Cycle(true,  true,  false, ZP_Y,     DATA,    true)
    ),
    ZERO_PAGE_Y_W("Zero Page, Y", 2, "$%02X,Y",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   DO,      true),
      new Cycle(true,  true,  false, PC,       IO,      true),
      new Cycle(true,  true,  false, ZP_Y,     DATA,    false)
    ),
    ABSOLUTE_X("Absolute, X", 3, "$%04X,X",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   AAL,     true),
      new Cycle(true,  true,  false, PC_INC,   AAH,     true),
      new Cycle(true,  true,  false, AA_X,     DATA,    true)
    ),
    ABSOLUTE_X_W("Absolute, X", 3, "$%04X,X",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   AAL,     true),
      new Cycle(true,  true,  false, PC_INC,   AAH,     true),
      new Cycle(true,  true,  false, PC,       IO,      true),
      new Cycle(true,  true,  false, AA_X,     DATA,    false)
    ),
    ABSOLUTE_X_RMW("Absolute, X", 3, "$%04X,X",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   AAL,     true),
      new Cycle(true,  true,  false, PC_INC,   AAH,     true),
      new Cycle(true,  true,  false, AA_X,     NULL,    true),
      new Cycle(true,  false, false, AA_X,     DATA,    true),
      new Cycle(true,  false, false, AA_X,     IO,      true), // Should read X+1
      new Cycle(true,  false, false, AA_X,     DATA,    false)
    ),
    ABSOLUTE_Y("Absolute, Y", 3, "$%04X,Y",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   AAL,     true),
      new Cycle(true,  true,  false, PC_INC,   AAH,     true),
      new Cycle(true,  true,  false, AA_Y,     DATA,    true)
    ),
    ABSOLUTE_Y_W("Absolute, Y", 3, "$%04X,Y",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   AAL,     true),
      new Cycle(true,  true,  false, PC_INC,   AAH,     true),
      new Cycle(true,  true,  false, AA_Y,     DATA,    false)
    ),
    RELATIVE("Relative", 2, "%d",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   DATA,    true)
    ),
    RELATIVE_BB("Relative", 3, "$%04X", // format is not ideal here.
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   DO,      true),
      new Cycle(true,  true,  false, ZP,       DATA,    true),
      new Cycle(true,  true,  false, ZP,       IO,      true),
      new Cycle(true,  true,  false, PC_INC,   DATA,    true) // this is not what the spec does.
    ),
    ABSOLUTE_INDIRECT("Absolute Indirect", 3, "($%04X)",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   AAL,     true),
      new Cycle(true,  true,  false, PC,       AAH,     true),
      new Cycle(true,  true,  false, PC,       AAH,     true),
      new Cycle(true,  true,  false, AA_INC,   NEW_PCL, true),
      new Cycle(true,  true,  false, AA,       NEW_PCH, true)
    ),
    STACK_INTERRUPT("Stack", 1, "",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC,       IO,      true),
      new Cycle(true,  true,  false, S_DEC,    PCH,     false),
      new Cycle(true,  true,  false, S_DEC,    PCL,     false),
      new Cycle(true,  true,  false, S_DEC,    P,       false),
      new Cycle(false, true,  false, VAL,      NEW_PCL, true),
      new Cycle(false, true,  false, VAH,      NEW_PCH, true)
    ),
    STACK_BRK("Stack", 2, "#$%02X", // BRK skips the next byte
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   IO,      true),
      new Cycle(true,  true,  false, S_DEC,    PCH,     false),
      new Cycle(true,  true,  false, S_DEC,    PCL,     false),
      new Cycle(true,  true,  false, S_DEC,    DATA,    false),
      new Cycle(false, true,  false, VAL,      NEW_PCL, true),
      new Cycle(false, true,  false, VAH,      NEW_PCH, true)
    ),
    STACK_RTI("Stack", 1, "",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC,       IO,      true),
      new Cycle(true,  true,  false, S,        IO,      true),
      new Cycle(true,  true,  false, S_INC,    P,       true),
      new Cycle(true,  true,  false, S_INC,    NEW_PCL, true),
      new Cycle(true,  true,  false, S_INC,    NEW_PCH, true)
    ),
    STACK_RTS("Stack", 1, "",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC,       IO,      true),
      new Cycle(true,  true,  false, S,        IO,      true),
      new Cycle(true,  true,  false, S_INC,    NEW_PCL, true),
      new Cycle(true,  true,  false, S_INC,    NEW_PCH, true),
      new Cycle(true,  true,  false, PC,       IO,      true)
    ),
    STACK_PUSH("Stack", 1, "",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC,       IO,      true),
      new Cycle(true,  true,  false, S_DEC,    DATA,    false)
      ),
    STACK_PULL("Stack", 1, "",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC,       IO,      true),
      new Cycle(true,  true,  false, PC,       IO,      true),
      new Cycle(true,  true,  false, S_INC,    DATA,    true)
    ),
    ABSOLUTE_INDEXED_INDIRECT("Absolute Indexed Indirect", 3, "($%04X,X)",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   AAL,     true),
      new Cycle(true,  true,  false, PC,       AAH,     true),
      new Cycle(true,  true,  false, PC,       IO,      true),
      new Cycle(true,  true,  false, AA_X,     NEW_PCL, true),
      new Cycle(true,  true,  false, AA_X_1,   NEW_PCH, true)
    ),
    ZERO_PAGE_INDIRECT("Zero Page Indirect", 2, "($%02X)",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   DO,      true),
      new Cycle(true,  true,  false, ZP_INC,   AAL,     true),
      new Cycle(true,  true,  false, ZP_INC,   AAH,     true),
      new Cycle(true,  true,  false, AA,       DATA,    true)
    ),
    ZERO_PAGE_INDIRECT_W("Zero Page Indirect", 2, "($%02X)",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   DO,      true),
      new Cycle(true,  true,  false, ZP_INC,   AAL,     true),
      new Cycle(true,  true,  false, ZP_INC,   AAH,     true),
      new Cycle(true,  true,  false, AA,       DATA,    false)
    ),
    NOP_1BYTE_1CYCLE("NOP", 1, "",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC,       NULL,    true) // Bug: should be one cycle.
    ),
    NOP_2BYTE_2CYCLE("NOP", 2, "",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   NULL,    true)
    ),
    NOP_2BYTE_3CYCLE("NOP", 2, "",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   NULL,    true),
      new Cycle(true,  true,  false, PC,       NULL,    true)
    ),
    NOP_2BYTE_4CYCLE("NOP", 2, "",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   NULL,    true),
      new Cycle(true,  true,  false, PC,       NULL,    true),
      new Cycle(true,  true,  false, PC,       NULL,    true)
    ),
    NOP_3BYTE_4CYCLE("NOP", 3, "",
      //        VPB    MLB    SYNC   Address   Data     RWB
      new Cycle(true,  true,  true,  PC_INC,   OP,      true),
      new Cycle(true,  true,  false, PC_INC,   NULL,    true),
      new Cycle(true,  true,  false, PC_INC,   NULL,    true),
      new Cycle(true,  true,  false, PC,       NULL,    true)
    ),
    NOP_3BYTE_8CYCLE("NOP", 3, "",
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
    private final int length;
    private final String format;
    private final Cycle[] cycles;
    AddressingMode(String modeName, int length, String format, Cycle... cycles) {
      this.modeName = modeName;
      this.length = length;
      this.format = format;
      this.cycles = cycles;
    }
    public String modeName() { return modeName; }
    public int length()      { return length; }
    public String format()   { return format; }
    public Cycle[] cycles()  { return cycles; }
  }

