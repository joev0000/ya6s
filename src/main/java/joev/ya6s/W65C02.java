package joev.ya6s;

import static joev.ya6s.W65C02.Step.*;
import static joev.ya6s.W65C02.Instruction.*;

/**
 * Simulate a cycle-accurate WDC W65C02S microprocessor.
 */
public class W65C02 {
  /* Status Flags */

  public static final byte NEGATIVE =          (byte)0b10000000;
  public static final byte OVERFLOW =          (byte)0b01000000;

  public static final byte BREAK =             (byte)0b00010000;
  public static final byte DECIMAL =           (byte)0b00001000;
  public static final byte INTERRUPT_DISABLE = (byte)0b00000100;
  public static final byte ZERO =              (byte)0b00000010;
  public static final byte CARRY =             (byte)0b00000001;

  /* Addressing Modes */

  // index X   --------------------------------------------+
  // index Y   -------------------------------------------+|
  // zero page ------------------------------------------+||
  // indirect  -----------------------------------------+|||
  // immediate ----------------------------------------+||||
  // relative  ---------------------------------------+|||||
  // implied   --------------------------------------+||||||
  //                                                 |||||||
  private static final byte ABSOLUTE =      (byte)0b00000000;
  private static final byte ABSOLUTE_X =    (byte)0b00000001;
  private static final byte ABSOLUTE_Y =    (byte)0b00000010;
  private static final byte ZERO_PAGE =     (byte)0b00000100;
  private static final byte ZERO_PAGE_X =   (byte)0b00000101;
  private static final byte ZERO_PAGE_Y =   (byte)0b00000110;
  private static final byte INDIRECT =      (byte)0b00001000;
  private static final byte INDIRECT_X =    (byte)0b00001001;
  private static final byte INDIRECT_ZP =   (byte)0b00001100;
  private static final byte INDIRECT_ZP_X = (byte)0b00001101;
  private static final byte INDEXED_ZP_Y =  (byte)0b00001110;
  private static final byte IMMEDIATE =     (byte)0b00010000;
  private static final byte RELATIVE =      (byte)0b00100000;
  private static final byte RELATIVE_ZP =   (byte)0b00100100;
  private static final byte IMPLIED =       (byte)0b01000000;
  private static final byte ILLEGAL =       (byte)0b10000000;

  private static final byte[] addressingModes = new byte[256];


  /**
   * The "micro-instructions" that are executed during a processor cycle.
   */
  enum Step {
    END,
    R_PC_OPERAND,
    R_PC_OPERAND_BRANCH,
    R_PC_ADL,
    R_PC_ADH,
    R_PC_ADZ,
    R_PC_PCH,
    R_PC_DISCARD,
    R_PC_DISCARD_INC,
    R_PC_DISCARD_SET_PC,
    R_FFFE_PCL,
    R_FFFF_PCH,
    RW_AD_OPERAND,
    R_AD_OPERAND,
    R_AD_DISCARD,
    R_AD_DISCARD_MODIFY,
    R_AD_AAL,
    R_AD_AAH,
    R_ADZ_X_AAL,
    R_ADZ_X_AAH,
    R_AD_X_AAL,
    R_AD_AAH_SET_PC,
    RW_AA_OPERAND,
    RW_AA_Y_OPERAND,
    RW_AD_X_OPERAND,
    RW_AD_Y_OPERAND,
    R_S_DISCARD,
    R_S_PCH,
    R_S_PCL,
    R_S_P,
    R_S_REG,
    W_S_PCH,
    W_S_PCL,
    W_S_P,
    W_S_REG,
    W_AD_OPERAND,
    R_PC_OPERAND_BRANCH_BIT
  }

  private static final Step[][] steps = new Step[256][];
  private static final Step[] S_IMMEDIATE = new Step[] {
    R_PC_OPERAND,
    END
  };
  private static final Step[] S_ABSOLUTE = new Step[] {
    R_PC_ADL,
    R_PC_ADH,
    RW_AD_OPERAND,
    END
  };
  private static final Step[] S_ABSOLUTE_RMW = new Step[] {
    R_PC_ADL,
    R_PC_ADH,
    R_AD_OPERAND,
    R_AD_DISCARD_MODIFY,
    W_AD_OPERAND,
    END
  };
  private static final Step[] S_ABSOLUTE_JMP = new Step[] {
    R_PC_ADL,
    R_PC_PCH,
    END
  };
  private static final Step[] S_ABSOLUTE_JSR = new Step[] {
    R_PC_ADL,
    R_S_DISCARD,
    W_S_PCH,
    W_S_PCL,
    R_PC_PCH,
    END
  };
  private static final Step[] S_ZERO_PAGE = new Step[] {
    R_PC_ADZ,
    RW_AD_OPERAND,
    END
  };
  private static final Step[] S_ZERO_PAGE_RMW = new Step[] {
    R_PC_ADZ,
    R_AD_OPERAND,
    R_AD_DISCARD_MODIFY,
    W_AD_OPERAND,
    END
  };
  private static final Step[] S_IMPLIED = new Step[] {
    R_PC_DISCARD,
    END
  };
  private static final Step[] S_STOP = new Step[] {
    R_PC_DISCARD,
    R_PC_DISCARD,
    END
  };
  private static final Step[] S_ZERO_PAGE_INDEXED = new Step[] {
    R_PC_ADZ,
    R_AD_AAL,
    R_AD_AAH,
    RW_AA_Y_OPERAND,
    END
  };
  private static final Step[] S_ZERO_PAGE_INDIRECT = new Step[] {
    R_PC_ADZ,
    R_PC_DISCARD,
    R_ADZ_X_AAL,
    R_ADZ_X_AAH,
    RW_AA_OPERAND,
    END
  };
  private static final Step[] S_ZERO_PAGE_X = new Step[] {
    R_PC_ADZ,
    R_PC_DISCARD,
    RW_AD_X_OPERAND,
    END
  };
  private static final Step[] S_ZERO_PAGE_X_RMW = new Step[] {
    R_PC_ADZ,
    R_PC_DISCARD,
    RW_AD_X_OPERAND,
    R_AD_DISCARD_MODIFY,
    W_AD_OPERAND,
    END
  };
  private static final Step[] S_ZERO_PAGE_Y = new Step[] {
    R_PC_ADZ,
    R_PC_DISCARD,
    RW_AD_Y_OPERAND,
    END
  };
  private static final Step[] S_ABSOLUTE_X = new Step[] {
    R_PC_ADL,
    R_PC_ADH,
    RW_AD_X_OPERAND,
    END
  };
  private static final Step[] S_ABSOLUTE_X_RMW = new Step[] {
    R_PC_ADL,
    R_PC_ADH,
    RW_AD_X_OPERAND,
    R_AD_DISCARD,
    R_AD_DISCARD_MODIFY,
    W_AD_OPERAND,
    END
  };
  private static final Step[] S_ABSOLUTE_Y = new Step[] {
    R_PC_ADL,
    R_PC_ADH,
    RW_AD_Y_OPERAND,
    END
  };
  private static final Step[] S_RELATIVE = new Step[] {
    R_PC_OPERAND_BRANCH,
    R_PC_DISCARD_SET_PC,
    END
  };
  private static final Step[] S_RELATIVE_BIT = new Step[] {
    R_PC_ADZ,
    R_AD_OPERAND,
    R_AD_DISCARD,
    R_PC_OPERAND_BRANCH_BIT,
    R_PC_DISCARD_SET_PC,
    END
  };
  private static final Step[] S_ABSOLUTE_INDIRECT = new Step[] {
    R_PC_ADL,
    R_PC_ADH,
    R_AD_AAL,
    R_AD_AAH_SET_PC,
    END
  };
  private static final Step[] S_STACK_BRK = new Step[] {
    R_PC_OPERAND,
    W_S_PCH,
    W_S_PCL,
    W_S_P,
    R_FFFE_PCL,
    R_FFFF_PCH,
    END
  };
  private static final Step[] S_STACK_RTI = new Step[] {
    R_PC_DISCARD,
    R_S_DISCARD,
    R_S_P,
    R_S_PCL,
    R_S_PCH,
    END
  };
  private static final Step[] S_STACK_RTS = new Step[] {
    R_PC_DISCARD,
    R_S_DISCARD,
    R_S_PCL,
    R_S_PCH,
    R_PC_DISCARD_INC,
    END
  };
  private static final Step[] S_STACK_PUSH = new Step[] {
    R_PC_DISCARD,
    W_S_REG,
    END
  };
  private static final Step[] S_STACK_PULL = new Step[] {
    R_PC_DISCARD,
    R_S_DISCARD,
    R_S_REG,
    END
  };
  private static final Step[] S_ABSOLUTE_INDEXED_INDIRECT = new Step[] {
    R_PC_ADL,
    R_PC_ADH,
    R_PC_DISCARD,
    R_AD_X_AAL,
    R_AD_AAH_SET_PC,
    END
  };

  /**
   * The list of instructions.
   */
  enum Instruction {
    ORA, AND, EOR, ADC, STA, LDA, CMP, SBC,
    ASL, ROL, LSR, ROR, DEC, INC,
    STX, LDX, STY, LDY, CPX, CPY, STZ,
    DEX, INX, DEY, INY,
    TXA, TAX, TYA, TAY, TXS, TSX,
    JMP, JSR, RTI, RTS,
    PHP, PLP, PHA, PLA, PHX, PLX, PHY, PLY,
    TSB, TRB, BIT,
    BPL, BMI, BVC, BVS, BCC, BCS, BNE, BEQ, BRA,
    CLC, SEC, CLI, SEI, CLD, SED, CLV,
    RMB, SMB, BBR, BBS,
    NOP, BRK, WAI, STP,
    XXX
  }

  /**
   * Array that maps opcodes to Instructions.
   */
  private final static Instruction[] instructions = new Instruction[] {
  // _0   _1   _2   _3   _4   _5   _6   _7   _8   _9   _A   _B   _C   _D   _E   _F
    BRK, ORA, XXX, XXX, TSB, ORA, ASL, RMB, PHP, ORA, ASL, XXX, TSB, ORA, ASL, BBR,
    BPL, ORA, ORA, XXX, TRB, ORA, ASL, RMB, CLC, ORA, INC, XXX, TRB, ORA, ASL, BBR,
    JSR, AND, XXX, XXX, BIT, AND, ROL, RMB, PLP, AND, ROL, XXX, BIT, AND, ROL, BBR,
    BMI, AND, AND, XXX, BIT, AND, ROL, RMB, SEC, AND, DEC, XXX, BIT, AND, ROL, BBR,

    RTI, EOR, XXX, XXX, XXX, EOR, LSR, RMB, PHA, EOR, LSR, XXX, JMP, EOR, LSR, BBR,
    BVC, EOR, EOR, XXX, XXX, EOR, LSR, RMB, CLI, EOR, PHY, XXX, XXX, EOR, LSR, BBR,
    RTS, ADC, XXX, XXX, STZ, ADC, ROR, RMB, PLA, ADC, ROR, XXX, JMP, ADC, ROR, BBR,
    BVS, ADC, ADC, XXX, STZ, ADC, ROR, RMB, SEI, ADC, PLY, XXX, JMP, ADC, ROR, BBR,

    BRA, STA, XXX, XXX, STY, STA, STX, SMB, DEY, BIT, TXA, XXX, STY, STA, STX, BBS,
    BCC, STA, STA, XXX, STY, STA, STX, SMB, TYA, STA, TXS, XXX, STZ, STA, STZ, BBS,
    LDY, LDA, LDX, XXX, LDY, LDA, LDX, SMB, TAY, LDA, TAX, XXX, LDY, LDA, LDX, BBS,
    BCS, LDA, LDA, XXX, LDY, LDA, LDX, SMB, CLV, LDA, TSX, XXX, LDY, LDA, LDX, BBS,

    CPY, CMP, XXX, XXX, CPY, CMP, DEC, SMB, INY, CMP, DEX, WAI, CPY, CMP, DEC, BBS,
    BNE, CMP, CMP, XXX, XXX, CMP, DEC, SMB, CLD, CMP, PHX, STP, XXX, CMP, DEC, BBS,
    CPX, SBC, XXX, XXX, CPX, SBC, INC, SMB, INX, SBC, NOP, XXX, CPX, SBC, INC, BBS,
    BEQ, SBC, SBC, XXX, XXX, SBC, INC, SMB, SED, SBC, PLX, XXX, XXX, SBC, SBC, BBS
  };

  /**
   * Build the mapping of opcodes to addressing modes, and opcodes to
   * micro-instruction steps.
   *
   * In general, the 6502 have four groups of instructions, determined by the
   * lower two bits.  For example, most arithmetic operations are in opcodes that
   * end in 01.
   */
  static {
    for(int opcode = 0; opcode < 256; opcode++) {
      byte addressingMode = ILLEGAL;
      switch(opcode & 0b00000011) {
        case 0b00:
          addressingMode = IMPLIED;
          switch(opcode & 0b00011100) {
            case 0b00100: addressingMode = ZERO_PAGE; break;
            case 0b01100: addressingMode = ABSOLUTE; break;
            case 0b10000: addressingMode = RELATIVE; break;
            case 0b10100: addressingMode = ZERO_PAGE_X; break;
          }

          // Patch odd opcodes
          switch(opcode) {
            case 0b00010100: addressingMode = ZERO_PAGE;  break; // TRB $xx
            case 0b00011100: addressingMode = ABSOLUTE;   break; // TRB $xxxx
            case 0b00100000: addressingMode = ABSOLUTE;   break; // JSR $xxxx
            case 0b00111100: addressingMode = ABSOLUTE_X; break; // BIT $xxxx,X
            case 0b01101100: addressingMode = INDIRECT;   break; // JMP ($xxxx)
            case 0b01111100: addressingMode = INDIRECT_X; break; // JMP ($xxxx,X)
            case 0b10000000: addressingMode = RELATIVE;   break; // BRA #$xx
            case 0b10011100: addressingMode = ABSOLUTE;   break; // STZ $xxxx
            case 0b10100000: addressingMode = IMMEDIATE;  break; // LDY #$xx
            case 0b10111100: addressingMode = ABSOLUTE_X; break; // LDY $xxxx,X
            case 0b11000000: addressingMode = IMMEDIATE;  break; // CPY #$xx
            case 0b11100000: addressingMode = IMMEDIATE;  break; // CPX #$xx
          }
          break;
        case 0b01:
          switch(opcode & 0b00011100) {
            case 0b00000: addressingMode = INDIRECT_ZP_X; break;
            case 0b00100: addressingMode = ZERO_PAGE; break;
            case 0b01000: addressingMode = IMMEDIATE; break;
            case 0b01100: addressingMode = ABSOLUTE; break;
            case 0b10000: addressingMode = INDEXED_ZP_Y; break;
            case 0b10100: addressingMode = ZERO_PAGE_X; break;
            case 0b11000: addressingMode = ABSOLUTE_Y; break;
            case 0b11100: addressingMode = ABSOLUTE_X; break;
          }
          break;
        case 0b10:
          switch(opcode & 0b00011100) {
            case 0b00000: addressingMode = IMMEDIATE; break;
            case 0b00100: addressingMode = ZERO_PAGE; break;
            case 0b01000: addressingMode = IMPLIED; break;
            case 0b01100: addressingMode = ABSOLUTE; break;
            case 0b10000: addressingMode = INDIRECT_ZP; break;
            case 0b10100: addressingMode = ZERO_PAGE_X; break;
            case 0b11000: addressingMode = IMPLIED; break;
            case 0b11100: addressingMode = ABSOLUTE_X; break;
          }

          // Patch odd opcodes
          switch(opcode) {
            case 0b10010110: addressingMode = ZERO_PAGE_Y; break; // STX $xx,Y
            case 0b10110110: addressingMode = ZERO_PAGE_Y; break; // LDX $xx,Y
            case 0b10111110: addressingMode = ABSOLUTE_Y;  break; // LDX $xxxx,Y
          }
          break;
        case 0b11:
          switch(opcode & 0b00011100) {
            case 0b00100, 0b10100: addressingMode = ZERO_PAGE; break;
            case 0b01000, 0b11000: addressingMode = IMPLIED;   break;
            case 0b01100, 0b11100: addressingMode = RELATIVE_ZP; break;
          }
          break;
      }
      addressingModes[opcode] = addressingMode;

      switch(addressingMode) {
        case IMMEDIATE: steps[opcode] = S_IMMEDIATE; break;
        case ABSOLUTE:
          switch(instructions[opcode]) {
            case BIT, STY, STZ, LDY, CPY, CPX, STX, LDX, ORA, AND, EOR, ADC, STA, LDA, CMP, SBC:
              steps[opcode] = S_ABSOLUTE; break;
            case ASL, ROL, LSR, ROR, INC, DEC, TSB, TRB:
              steps[opcode] = S_ABSOLUTE_RMW; break;
            case JMP:
              steps[opcode] = S_ABSOLUTE_JMP; break;
            case JSR:
              steps[opcode] = S_ABSOLUTE_JSR; break;
            default:
          }
          break;
        case ZERO_PAGE:
          switch(instructions[opcode]) {
            case BIT, STY, STZ, LDY, CPY, CPX, STX, LDX, ORA, AND, EOR, ADC, STA, LDA, CMP, SBC:
              steps[opcode] = S_ZERO_PAGE; break;
            case ASL, ROL, LSR, ROR, INC, DEC, TSB, TRB, RMB, SMB:
              steps[opcode] = S_ZERO_PAGE_RMW; break;
            default:
          }
          break;
        case IMPLIED:
          switch(instructions[opcode & 0xFF]) {
            case PHA, PHX, PHY, PHP: steps[opcode] = S_STACK_PUSH; break;
            case PLA, PLX, PLY, PLP: steps[opcode] = S_STACK_PULL; break;
            default: steps[opcode] = S_IMPLIED;
          }
          break;
        case INDEXED_ZP_Y: steps[opcode] = S_ZERO_PAGE_INDEXED; break;
        case INDIRECT_ZP_X: steps[opcode] = S_ZERO_PAGE_INDIRECT; break;
        case ZERO_PAGE_X:
          switch(instructions[opcode]) {
            case ASL, ROL, LSR, ROR, INC, DEC:
              steps[opcode] = S_ZERO_PAGE_X_RMW; break;
            default:
            steps[opcode] = S_ZERO_PAGE_X; break;
          }
          break;
        case ZERO_PAGE_Y: steps[opcode] = S_ZERO_PAGE_Y; break;
        case ABSOLUTE_X:
          switch(instructions[opcode]) {
            case ASL, ROL, LSR, ROR, INC, DEC:
              steps[opcode] = S_ABSOLUTE_X_RMW; break;
            default:
              steps[opcode] = S_ABSOLUTE_X; break;
          }
          break;
        case ABSOLUTE_Y: steps[opcode] = S_ABSOLUTE_Y; break;
        case RELATIVE: steps[opcode] = S_RELATIVE; break;
        case RELATIVE_ZP: steps[opcode] = S_RELATIVE_BIT; break;
        case INDIRECT: steps[opcode] = S_ABSOLUTE_INDIRECT; break;
        case INDIRECT_X: steps[opcode] = S_ABSOLUTE_INDEXED_INDIRECT; break;
      }
    }
    steps[0x00] = S_STACK_BRK;
    steps[0x40] = S_STACK_RTI;
    steps[0x60] = S_STACK_RTS;
    steps[0xDB] = S_STOP;
  }
  private short pc;
  private byte a;
  private byte x;
  private byte y;
  private byte s;
  private byte p = (byte)0b00100000; // The magic bit 5.

  private byte operand;
  private boolean stopped = false;
  private boolean waiting = false;

  private final byte[] memory = new byte[65536]; // TODO: this is temporary
  public byte[] memory() { return memory; }      // TODO: this is temporary

  public short pc() { return pc; }
  public byte a() { return a; }
  public byte x() { return x; }
  public byte y() { return y; }
  public byte s() { return s; }
  public byte p() { return p; }
  public boolean stopped() { return stopped; }
  public boolean waiting() { return waiting; }

  /**
   * Reset the processor: set the PC to the value at FFFC/D.
   */
  public void reset() {
    byte pc_lo = read((short)0xFFFC);
    byte pc_hi = read((short)0xFFFD);
    pc = (short)(pc_hi << 8 | pc_lo);
  }

  /**
   * Read the byte at the given address.
   *
   * @param address the address to read
   * @return the value at the address
   */
  public byte read(short address) {
    //System.out.format("Reading $%04X: $%02X\n", address, memory[address & 0xFFFF]);
    return memory[address & 0xFFFF];
  }

  /**
   * Read the byte at the given address.
   *
   * @param address_lo the low byte of the address to read
   * @param address_hi the low byte of the address to read
   * @return the value at the address
   */
  private byte read(byte address_lo, byte address_hi) {
    return read((short)(address_hi << 8 | (address_lo & 0xFF)));
  }

  /**
   * Write the byte to the given address
   *
   * @param address the address to write
   * @param value the value to write
   */
  public void write(short address, byte value) {
    //System.out.format("Writing $%04X: $%02X\n", address, value);
    memory[address & 0xFFFF] = value;
  }

  /**
   * Write the byte to the given address
   *
   * @param address_lo the low byte of the address to write
   * @param address_hi the low byte of the address to write
   * @param value the value to write.
   */
  private void write(byte address_lo, byte address_hi, byte value) {
    write((short)(address_hi << 8 | (address_lo & 0xFF)), value);
  }

  //no1bdizc
  /**
   * Get a String representation of the current status bits
   *
   * Uppercase indicates the bit is set, lowercase indicates the
   * bit is clear.  Bit 5 ("reserved") is represenated by a hyphen.
   *
   * @return the status of the processor.
   */
  public String status() {
    return String.format("%c%c-%c%c%c%c%c",
      (p & NEGATIVE)          == 0 ? 'n' : 'N',
      (p & OVERFLOW)          == 0 ? 'o' : 'O',
      (p & BREAK)             == 0 ? 'b' : 'B',
      (p & DECIMAL)           == 0 ? 'd' : 'D',
      (p & INTERRUPT_DISABLE) == 0 ? 'i' : 'I',
      (p & ZERO)              == 0 ? 'z' : 'Z',
      (p & CARRY)             == 0 ? 'c' : 'C');
  }

  public int step = -1;
  private Instruction instruction = NOP;
  byte opcode;
  byte adl;
  byte adh;
  byte aal;
  byte aah;

  /**
   * RUn a single cycle of the processor.
   */
  public void tick() {
    if(step == -1) {
      // Initial micro-instruction.  If the previous instruction included
      // a computation, perform that here.  THis simulates the "pipelining"
      // in the 6502, which does the computation at the same time it fetches
      // the next opcode..
      switch(instruction) {
        case STP: stopped = true; break;
        case CLC: p &= ~CARRY; break;
        case SEC: p |=  CARRY; break;
        case CLD: p &= ~DECIMAL; break;
        case SED: p |=  DECIMAL; break;
        case CLI: p &= ~INTERRUPT_DISABLE; break;
        case SEI: p |=  INTERRUPT_DISABLE; break;
        case CLV: p &= ~OVERFLOW; break;
        case DEX: x--; setNZ(x); break;
        case INX: x++; setNZ(x); break;
        case DEY: y--; setNZ(y); break;
        case INY: y++; setNZ(y); break;
        case TAX: x = a; setNZ(a); break;
        case TXA: a = x; setNZ(a); break;
        case TAY: y = a; setNZ(a); break;
        case TYA: a = y; setNZ(a); break;
        case TSX: x = s; setNZ(x); break;
        case TXS: s = x; setNZ(x); break;
        case ASL: setCIf((a & 0x80) != 0); a <<= 1; setNZ(a); break;
        case ROL: int b0 = carry() ? 1 : 0; setCIf((a & 0x80) != 0); a <<= 1; a |= b0; setNZ(a); break;
        case ROR: int b7 = carry() ? 0x80 : 0; setCIf((a & 0x01) != 0); a >>= 1; a |= b7; setNZ(a); break;
        case LSR: setCIf((a & 0x01) != 0); a >>= 1; setNZ(a); break;
        case INC: a++; setNZ(a); break;
        case DEC: a--; setNZ(a); break;
        case ADC:
          if((p & DECIMAL) == 0) {
            a += operand + (carry() ? 1 : 0);
            setCV((byte)(a - operand - (carry() ? 1 : 0)), operand, a);
          }
          else {
            // add low digits, check for carry
            // add high digits plus carry...
            byte lo = (byte)((a & 0x0F) + (operand & 0x0F) + (carry() ? 1 : 0));
            if(lo >= 0x0A) { lo += 0x06; lo &= 0x1F; }
            a = (byte)((a & 0xF0) + (operand & 0xF0) + lo);
            if(((a >> 4) & 0x0F) >= 0x0A) { a += 0x60; p |= CARRY; } else { p &= ~CARRY; }
            // What about OVERFLOW?
          }
          setNZ(a);

          break;
        case SBC:
          if((p & DECIMAL) == 0) {
            a += ~operand + (carry() ? 1 : 0);
            setCV((byte)(a - (~operand) - (carry() ? 1 : 0)), operand, a);
          }
          else {
          }
          setNZ(a);
          break;
        case AND: a &= operand; setNZ(a); break;
        case ORA: a |= operand; setNZ(a); break;
        case EOR: a ^= operand; setNZ(a); break;
        case LDA: a = operand; setNZ(a); break;
        case LDX: x = operand; setNZ(x); break;
        case LDY: y = operand; setNZ(y); break;
        case CMP: setNZ((byte)(a - operand)); setCIf(a >= operand); break;
        case CPX: setNZ((byte)(x - operand)); setCIf(x >= operand); break;
        case CPY: setNZ((byte)(y - operand)); setCIf(y >= operand); break;
        case BIT:
          p = (byte)((a & operand) == 0 ? p | ZERO : p & ~ZERO);
          p = (byte)((operand & 0x40) != 0 ? p | OVERFLOW : p & ~OVERFLOW);
          p = (byte)((operand & 0x80) != 0 ? p | NEGATIVE : p & ~NEGATIVE);
          break;
        default:
      }
      opcode = read(pc++);
      step = 0;
      return;
    }

    // find and run the next micro-instruction
    switch(steps[opcode & 0xFF][step]) {
      case R_PC_OPERAND: operand = read(pc++); break;
      case R_PC_DISCARD: read(pc); break;
      case R_PC_DISCARD_INC: read(pc++); break;
      case R_PC_ADL:     adl = read(pc++); break;
      case R_PC_ADH:     adh = read(pc++); break;
      case R_PC_ADZ:     adl = read(pc++); adh = 0; break;
      case RW_AD_OPERAND:
        switch(instructions[opcode & 0xFF]) {
          case STA, STX, STY:
            write(adl, adh, operand); break;
          case STZ:
            write(adl, adh, (byte)0); break;
        default:
          operand = read(adl, adh);
        }
        break;
      case R_AD_OPERAND: operand = read(adl, adh); break;
      case R_AD_DISCARD: read(adl, adh); break;
      case R_AD_DISCARD_MODIFY:
        read(adl, adh);
        switch(instructions[opcode & 0xFF]) {
          case ASL: setCIf((operand & 0x80) != 0); operand <<= 1; setNZ(operand); break;
          case ROL: int b0 = carry() ? 1 : 0; setCIf((operand & 0x80) != 0); operand <<= 1; operand |= b0; setNZ(operand); break;
          case LSR: setCIf((operand & 0x01) != 0); operand >>= 1; setNZ(operand); break;
          case ROR: int b7 = carry() ? 0x80 : 0; setCIf((operand & 0x01) != 0); operand >>= 1; operand |= b7; setNZ(operand); break;
          case INC: operand++; setNZ(operand); break;
          case DEC: operand--; setNZ(operand); break;
          case TSB: p = (byte)((a & operand) == 0 ? p | ZERO : p & ~ZERO); operand |= a;  break;
          case TRB: p = (byte)((a & operand) == 0 ? p | ZERO : p & ~ZERO); operand &= ~a; break;
          case RMB: operand &= ~(1 << ((opcode & 0x70) >> 4)); break;
          case SMB: operand |= 1 << ((opcode & 0x70) >> 4); break;
          default:
        }
        break;
      case W_AD_OPERAND: write(adl, adh, operand); break;
      case R_PC_PCH: pc = (short)((adl & 0xFF) | (read(pc++) << 8)); break;
      case R_S_DISCARD: read((short)(0x100 + s)); break;
      case W_S_PCH: write((short)(0x100 + s), (byte)((pc >> 8) & 0xFF)); s--; break;
      case W_S_PCL: write((short)(0x100 + s), (byte)(pc & 0xFF)); s--; break;
      case W_S_P: write((short)(0x100 + s), (byte)(p | BREAK)); s--; break;
      case W_S_REG:
        switch(instructions[opcode & 0xFF]) {
          case PHA: operand = a; break;
          case PHX: operand = x; break;
          case PHY: operand = y; break;
          case PHP: operand = (byte)(p | BREAK); break;
          default:
        }
        write((short)(0x100 + s), operand); s--;
        break;
      case R_S_PCH: s++; pc = (short)((read((short)(0x100 + s)) << 8) | (byte)(pc & 0xFF)); break;
      case R_S_PCL: s++; pc = (short)((pc & 0xFF00) | (read((short)(0x100 + s)) & 0xFF)); break;
      case R_S_P: s++; p = (byte)((read((short)(0x100 + s)) | 0b00100000) & ~BREAK); break;
      case R_S_REG:
        s++;
        operand = read((short)(0x100 + s));
        switch(instructions[opcode & 0xFF]) {
          case PLA: a = operand; setNZ(operand); break;
          case PLX: x = operand; setNZ(operand); break;
          case PLY: y = operand; setNZ(operand); break;
          case PLP: p = (byte)(operand | 0b0010000); break;
          default:
        }
        break;
      case R_FFFE_PCL: pc = (short)((pc & 0xFF00) | (read((short)0xFFFE) & 0xFF)); break;
      case R_FFFF_PCH: pc = (short)((read((short)0xFFFF) << 8) | (pc & 0xFF)); break;
      case R_AD_AAL: aal = read(adl++, adh); if(adl == 0) { adh++; } break;
      case R_AD_AAH: aah = read(adl,   adh); break;
      case R_AD_AAH_SET_PC: aah = read(adl, adh); pc = (short)((aah << 8) | (aal & 0xFF)); break;
      case RW_AA_Y_OPERAND:
        if((((y ^ aal) & ~(aal + y)) | (y & aal) & 0x80) != 0)
          aah++;  // TODO: page boundary?
        aal += y;
        switch(instructions[opcode & 0xFF]) {
          case STA, STX, STY: write(aal, aah, operand); break;
          case STZ:           write(aal, aah, (byte)0); break;
          default:            operand = read(aal, aah);
        }
        break;
      case R_ADZ_X_AAL: adl += x; aal = read(adl, (byte)0); adl++; break;
      case R_ADZ_X_AAH: aah = read(adl, (byte)0); break;
      case R_AD_X_AAL:
        if(wouldCarry(adl, x)) adh++;
        adl += x;
        aal = read(adl++, adh);
        if(adl == 0) adh++;
        break;
      case RW_AA_OPERAND:
        switch(instructions[opcode & 0xFF]) {
          case STA, STX, STY: write(aal, aah, operand); break;
          case STZ:           write(aal, aah, (byte)0); break;
          default:            operand = read(aal, aah);
        }
        break;
      case RW_AD_X_OPERAND:
        if(((((adl & x) | ((adl ^ x) & ~(adl + x))) & 0x80) != 0) &&
            (addressingModes[opcode & 0xFF] & 0x40) == 0) {
          adh++;
          // TODO add page boundary cycle
        }
        adl += x;
        switch(instructions[opcode & 0xFF]) {
          case STA, STX, STY: write(adl, adh, operand); break;
          case STZ:           write(adl, adh, (byte)0); break;
          default:            operand = read(adl, adh); break;
        }
        break;
      case RW_AD_Y_OPERAND:
        if(((((adl & y) | ((adl ^ y) & ~(adl + y))) & 0x80) != 0) &&
            (addressingModes[opcode & 0xFF] & 0x40) == 0) {
          adh++;
          // TODO add page boundary cycle
        }
        adl += y;
        switch(instructions[opcode & 0xFF]) {
          case STA, STX: write(adl, adh, operand); break;
          default: operand = read(adl, adh);
        }
        break;

      case R_PC_OPERAND_BRANCH:
        Instruction i = instructions[opcode & 0xFF];
        operand = read(pc++);
        if( i == BRA ||
           (i == BNE && ((p & ZERO)     == 0)) ||
           (i == BEQ && ((p & ZERO)     != 0)) ||
           (i == BCC && ((p & CARRY)    == 0)) ||
           (i == BCS && ((p & CARRY)    != 0)) ||
           (i == BPL && ((p & NEGATIVE) == 0)) ||
           (i == BMI && ((p & NEGATIVE) != 0)) ||
           (i == BVC && ((p & OVERFLOW) == 0)) ||
           (i == BVS && ((p & OVERFLOW) != 0))) {
          adl = (byte)((pc + operand) & 0xFF);
          adh = (byte)((pc + operand) >> 8);
          if(adh != (byte)((pc >> 8) & 0xFF)) {
            // TODO: deal with page boundary crossing.
          }
        }
        else {
          step++;
        }
        break;
      case R_PC_DISCARD_SET_PC:
        read(pc);
        pc = (short)((adh << 8) | (adl & 0xFF));
        break;
      case R_PC_OPERAND_BRANCH_BIT:
        int bit = ((opcode & 0b01110000) >> 4) & 0x07;
        operand >>= bit;
        operand &= 0x01;
        if((((opcode & 0x80) == 0) && (operand == 0)) ||
            ((opcode & 0x80) != 0) && (operand != 0)) {
          operand = read(pc++);
          adl = (byte)((pc + operand) & 0xFF);
          adh = (byte)((pc + operand) >> 8);
          if(adh != (byte)((pc >> 8) & 0xFF)) {
            // TODO: deal with page boundary crossing.
          }
        }
        else {
          read(pc++);
          step++;
        }
        break;
      case END:
        instruction = instructions[opcode & 0xFF];
        switch(instruction) {
          case ROL, ROR, ASL, LSR, INC, DEC, TSB, TRB:
            if(addressingModes[opcode & 0xFF] != IMPLIED) {
              instruction = NOP;
            }
            break;
          default:
        }
        step = -1;
        tick();
        //System.out.format("PC: $%04X, A: $%02X, X: $%02X, Y: $%02X, S: $%02X, P: $%02X (%s)%n", pc-1, a, x, y, s, p, status());
        step = -1;
        break;
      default:
        System.out.println("Step not yet implemented.  Stopping processor."); stopped=true;
    }
    step++;
  }

  /**
   * Return true if adding these numbers would resut in a carry.
   *
   * Checks the MSB of the pameter, using the formula
   *
   *  (a & b) | ((a ^ b) & ~(a + b))
   *
   * If the bits are both 1, then we have a carry out.
   * If the bits are both 0, then we cannot have a carry out.
   * If one of the bits is 1, and the sum is 1, then we do not have a carry out.
   * If one of the buts is 1, and the sum is 0, then we have a carry out: the
   *   only way this happens is if there was a carry in.
   *
   * @param a the addend
   * @param b the addend
   * @return true if adding the parameters would result in a carry.
   */
  private boolean wouldCarry(byte a, byte b) {
    return (((a & b) | ((a ^ b) & ~(a + b))) & 0x80) != 0;
  }

  /**
   * Set or clear the Negative and Zero flags based on the given value.
   *
   * @param b the byte to use to set the Negative and Zero flags.
   */
  private void setNZ(byte b) {
    p = (b & (byte)0b10000000) != 0 ? (byte)(p | NEGATIVE) : (byte)(p & ~NEGATIVE);
    p = (b == 0) ? (byte)(p | ZERO) : (byte)(p & ~ZERO);
  }

  /**
   * Sets the Carry flag if the boolean is true, clears it otherwise.
   *
   * @param value the condition
   */
  private void setCIf(boolean value) {
    p = value ? (byte)(p | CARRY) : (byte)(p & ~CARRY);
  }

  /**
   * Set or clear the Carry and Overflow flags during an ADC or SBC
   * instruction.
   *
   * @param a the first operand
   * @param b the second operand
   * @param result the addition or subtraction of the operands
   */
  private void setCV(byte a, byte b, byte result) {
    p = (((a & b) | ((a ^ b) & ~result)) & (byte)0x80) != 0 ? (byte)(p | CARRY) : (byte)(p & ~CARRY);
    p = ((a ^ result) & (b ^ result) & (byte)0x80) != 0 ? (byte)(p | OVERFLOW) : (byte)(p & ~OVERFLOW);
  }

  /**
   * Return true if the Carry flag is set.
   *
   * @return true if the Carry flag is set.
   */
  private boolean carry() {
    return (p & (byte)0b1) != 0;
  }
}
