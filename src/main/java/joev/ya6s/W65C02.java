package joev.ya6s;

import static joev.ya6s.W65C02.HalfStep.*;
import static joev.ya6s.W65C02.Instruction.*;

import joev.ya6s.signals.Bus;
import joev.ya6s.signals.Signal;

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

  enum HalfStep {
    D_OPCODE,
    R_PC,
    R_PC_INC,
    R_PC_SYNC,
    R_PC_INC_SYNC,
    R_PC_AD_SYNC,
    R_AD, R_AD_INC, R_AD_X, R_AD_X_INC,
    R_FFFC, R_FFFD,
    D_OPERAND,
    D_ADL, D_ADH, D_ADZ,
    D_AAL, D_AAH,
    D_PCL, D_PCH,
    D_P,
    PCH_D, PCL_D,
    R_S, R_S_DEC, R_S_INC, R_S_PC_INC,
    W_S, W_S_DEC,
    RW_AD_REG, RW_AD_X_REG, RW_AD_Y_REG,
    RW_AA, RW_AA_Y,
    MODIFY,
    NOOP
  };
  private static final HalfStep[][] halfsteps = new HalfStep[256][];

  private static final HalfStep[] HS_IMMEDIATE = new HalfStep[] {
    D_OPCODE,  R_PC_INC,
    D_OPERAND, R_PC_INC_SYNC
  };
  private static final HalfStep[] HS_ABSOLUTE = new HalfStep[] {
    D_OPCODE,  R_PC_INC,
    D_ADL,     R_PC_INC,
    D_ADH,     RW_AD_REG,
    D_OPERAND, R_PC_INC_SYNC
  };
  private static final HalfStep[] HS_ABSOLUTE_RMW = new HalfStep[] {
    D_OPCODE,  R_PC_INC,
    D_ADL,     R_PC_INC,
    D_ADH,     R_AD,
    D_OPERAND, R_AD,
    MODIFY,    RW_AD_REG,
    NOOP,      R_PC_INC_SYNC
  };
  private static final HalfStep[] HS_ABSOLUTE_JUMP = new HalfStep[] {
    D_OPCODE, R_PC_INC,
    D_ADL,    R_PC_INC,
    D_ADH,    R_PC_AD_SYNC
  };
  private static final HalfStep[] HS_ABSOLUTE_JSR = new HalfStep[] {
    D_OPCODE, R_PC_INC,
    D_ADL,    R_S_PC_INC,
    PCH_D,    W_S,
    PCL_D,    W_S_DEC,
    NOOP,     R_PC,
    D_ADH,    R_PC_AD_SYNC
  };
  private static final HalfStep[] HS_ZERO_PAGE = new HalfStep[] {
    D_OPCODE,  R_PC_INC,
    D_ADZ,     RW_AD_REG,
    D_OPERAND, R_PC_INC_SYNC
  };
  private static final HalfStep[] HS_ZERO_PAGE_RMW = new HalfStep[] {
    D_OPCODE,  R_PC_INC,
    D_ADZ,     R_AD,
    D_OPERAND, R_AD,
    MODIFY,    RW_AD_REG,
    NOOP,      R_PC_INC_SYNC
  };
  private static final HalfStep[] HS_IMPLIED = new HalfStep[] {
    D_OPCODE, R_PC,
    NOOP,     R_PC_INC_SYNC
  };
  private static final HalfStep[] HS_IMPLIED_RTS = new HalfStep[] {
    D_OPCODE, R_PC_INC,
    NOOP,     R_S,
    NOOP,     R_S,
    D_PCL,    R_S_INC,
    D_PCH,    R_PC,
    NOOP,     R_PC_INC_SYNC
  };
  private static final HalfStep[] HS_ZERO_PAGE_INDEXED = new HalfStep[] {
    D_OPCODE , R_PC_INC,
    D_ADZ,     R_AD,
    D_AAL,     R_AD_INC,
    D_AAH,     RW_AA_Y,
    D_OPERAND, R_PC_INC_SYNC
  };
  private static final HalfStep[] HS_ZERO_PAGE_INDIRECT = new HalfStep[] {
    D_OPCODE,  R_PC_INC,
    D_ADZ,     R_PC,
    NOOP,      R_AD_X,
    D_AAL,     R_AD_X_INC,
    D_AAH,     RW_AA,
    D_OPERAND, R_PC_INC_SYNC
  };
  private static final HalfStep[] HS_ZERO_PAGE_X = new HalfStep[] {
    D_OPCODE,  R_PC_INC,
    D_ADZ,     R_PC,
    NOOP,      RW_AD_X_REG,
    D_OPERAND, R_PC_INC_SYNC
  };
  private static final HalfStep[] HS_ZERO_PAGE_X_RMW = new HalfStep[] {
    D_OPCODE,  R_PC_INC,
    D_ADZ,     R_PC,
    NOOP,      R_AD_X,
    D_OPERAND, R_AD_X,
    MODIFY,    RW_AD_X_REG,
    NOOP,      R_PC_INC_SYNC
  };
  private static final HalfStep[] HS_ZERO_PAGE_Y = new HalfStep[] {
    D_OPCODE,  R_PC_INC,
    D_ADZ,     R_PC,
    NOOP,      RW_AD_Y_REG,
    D_OPERAND, R_PC_INC_SYNC
  };
  private static final HalfStep[] HS_STOP = new HalfStep[] {
    D_OPCODE, R_PC_INC,
    NOOP,     R_PC,
    NOOP,     R_PC_SYNC
  };
  private static final HalfStep[] HS_RESET = new HalfStep[] {
    NOOP,  R_PC,
    NOOP,  R_S_DEC,
    D_PCH, R_S_DEC,
    D_PCL, R_S_DEC,
    D_P,   R_FFFC,
    D_PCL, R_FFFD,
    D_PCH, R_PC_SYNC
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
        case IMMEDIATE: halfsteps[opcode] = HS_IMMEDIATE; break;
        case IMPLIED:
          switch(instructions[opcode]) {
            case STP: halfsteps[opcode] = HS_STOP; break;
            case RTS: halfsteps[opcode] = HS_IMPLIED_RTS; break;
            default:  halfsteps[opcode] = HS_IMPLIED;
          }
          break;
        case ABSOLUTE:
          switch(instructions[opcode]) {
            case ASL, ROL, LSR, ROR, INC, DEC:
                      halfsteps[opcode] = HS_ABSOLUTE_RMW; break;
            case JMP: halfsteps[opcode] = HS_ABSOLUTE_JUMP; break;
            case JSR: halfsteps[opcode] = HS_ABSOLUTE_JSR; break;
            default:  halfsteps[opcode] = HS_ABSOLUTE;
          }
          break;
        case ZERO_PAGE:
          switch(instructions[opcode]) {
            case ASL, ROL, LSR, ROR, INC, DEC, RMB, SMB:
                     halfsteps[opcode] = HS_ZERO_PAGE_RMW; break;
            default: halfsteps[opcode] = HS_ZERO_PAGE;
          }
          break;
        case INDEXED_ZP_Y: halfsteps[opcode] = HS_ZERO_PAGE_INDEXED; break;
        case INDIRECT_ZP_X: halfsteps[opcode] = HS_ZERO_PAGE_INDIRECT; break;
        case ZERO_PAGE_X:
          switch(instructions[opcode]) {
            case ASL, ROL, LSR, ROR, DEC, INC:
                     halfsteps[opcode] = HS_ZERO_PAGE_X_RMW; break;
            default: halfsteps[opcode] = HS_ZERO_PAGE_X;
          }
          break;
        case ZERO_PAGE_Y: halfsteps[opcode] = HS_ZERO_PAGE_Y; break;
      }
    }
  }
  // ----- END OF STATIC SECTION

  private short pc;
  private byte a;
  private byte x;
  private byte y;
  private byte s;
  private byte p = (byte)0b00100000; // The magic bit 5.

  private int halfstep = 0;
  private byte opcode;
  private byte adl;
  private byte adh;
  private byte aal;
  private byte aah;

  private byte operand;
  private boolean stopped = false;
  private boolean waiting = false;
  private boolean resetting = false;

  private final Signal phi2;
  private final Bus address;
  private final Bus data;
  private final Signal rwb;
  private final Signal sync;
  private final Signal resb;
  private final Signal rdy;

  private Signal.Listener tickFn = this::tick;

  public W65C02(Backplane backplane) {
    phi2 = backplane.clock();
    address = backplane.address();
    data = backplane.data();
    rwb = backplane.rwb();
    sync = backplane.sync();
    resb = new Signal("resb");
    rdy = new Signal("rdy");
    rdy.value(true);
    resetting = true;
    phi2.register(tickFn);
    rdy.register(et -> {
      if(et == Signal.EventType.POSITIVE_EDGE) {
        phi2.register(tickFn);
      }
      else {
        phi2.unregister(tickFn);
      }
    });
  }
  public Signal resb() { return resb; }
  public Signal rdy() { return rdy; }

  public short pc() { return pc; }
  public byte a() { return a; }
  public byte x() { return x; }
  public byte y() { return y; }
  public byte s() { return s; }
  public byte p() { return p; }
  public boolean stopped() { return stopped; }
  public boolean waiting() { return waiting; }

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

  /**
   * RUn a single cycle of the processor.
   */
  @SuppressWarnings("fallthrough")
  public void tick(Signal.EventType eventType) {
    if(stopped || eventType != Signal.EventType.NEGATIVE_EDGE)
      return;
    for(int half = 0; half < 2; half++) {
      HalfStep hs;
      if(resetting) {
        hs = HS_RESET[halfstep];
        System.out.format("tick%d: RESETTING: halfstep: %d (%s)%n", half, halfstep, hs);
      }
      else {
        hs = halfsteps[opcode & 0xFF][halfstep];
        System.out.format("tick%d: opcode: %02X (%s), halfstep: %d (%s)%n", half, (opcode & 0xFF), instructions[opcode & 0xFF], halfstep, hs);
      }
      switch(hs) {
        case D_OPCODE:
          opcode = (byte)data.value(); sync.value(false); break;
        case D_OPERAND: operand = (byte)data.value(); break;
        case D_PCL: pc = (short)((pc & 0xFF00) | (byte)data.value()); break;
        case D_PCH: pc = (short)((data.value() << 8) | (pc & 0x00FF)); break;
        case D_ADZ: adh = 0; // intentional fallthrough to D_ADL
        case D_ADL: adl = (byte)data.value(); break;
        case D_ADH: adh = (byte)data.value(); break;
        case D_AAL: aal = (byte)data.value(); break;
        case D_AAH: aah = (byte)data.value(); break;
        case D_P: p = (byte)(data.value() | 0x20); break;
        case PCH_D: data.value((byte)((pc >> 8) & 0xFF)); break;
        case PCL_D: data.value((byte)(pc & 0xFF)); break;
        case R_PC: address.value(pc); rwb.value(true); break;
        case R_PC_INC: address.value(++pc); rwb.value(true); break;
        case R_AD_INC: adl++; if(adl == 0) adh++; // intentional fallthrough to R_AD
        case R_AD:
          address.value((adh << 8) | (adl & 0xFF)); rwb.value(true); break;
        case R_AD_X_INC: adl++; // intentional fallthrough to R_AD_X
        case R_AD_X: address.value((adh << 0) | ((adl + x) & 0xFF)); rwb.value(true); break;
        case RW_AD_REG:
          switch(instructions[opcode & 0xFF]) {
            case STX: data.value(x); rwb.value(false); break;
            case STY: data.value(y); rwb.value(false); break;
            case STA: data.value(a); rwb.value(false); break;
            case STZ: data.value(0); rwb.value(false); break;
            case ASL, ROL, LSR, ROR, INC, DEC, RMB, SMB:
                      data.value(operand); rwb.value(false); break;
            default: rwb.value(true);
          }
          address.value((adh << 8) | (adl & 0xFF));
          break;
        case RW_AD_X_REG:
          switch(instructions[opcode & 0xFF]) {
            case STY: data.value(y); rwb.value(false); break;
            case STA: data.value(a); rwb.value(false); break;
            case STZ: data.value(0); rwb.value(false); break;
            case ASL, ROL, LSR, ROR, INC, DEC, RMB, SMB:
                      data.value(operand); rwb.value(false); break;
            default: rwb.value(true);
          }
          address.value((adh << 8) | ((adl + x) & 0xFF));
          break;
        case RW_AD_Y_REG:
          switch(instructions[opcode & 0xFF]) {
            case STX: data.value(x); rwb.value(false); break;
            default: rwb.value(true);
          }
          address.value((adh << 8) | ((adl + y) & 0xFF));
          break;
        case RW_AA_Y:
          switch(instructions[opcode & 0xFF]) {
            case STX: data.value(x); rwb.value(false); break;
            case STA: data.value(a); rwb.value(false); break;
            default: rwb.value(true);
          }
          if(wouldCarry(aal, y)) aah++;   // TODO: page boundary?
          address.value((aah << 8) | ((aal + y) & 0xFF));
          break;
        case RW_AA:
          switch(instructions[opcode & 0xFF]) {
            case STA: data.value(a); rwb.value(false); break;
            default: rwb.value(true);
          }
          address.value((aah << 8) | (aal & 0xFF));
          break;
        case R_S_PC_INC: pc++; // Intentional fallthrough to R_S
        case R_S: address.value(0x100  | (s & 0xFF)); rwb.value(true); break;
        case R_S_DEC: address.value(0x100 | ((s--) & 0xFF)); rwb.value(true); break;
        case R_S_INC: address.value(0x100 | ((++s) & 0xFF)); rwb.value(true); break;
        case W_S: address.value(0x100 | (s & 0xFF)); rwb.value(false); break;
        case W_S_DEC: address.value(0x100 | ((--s) & 0xFF)); rwb.value(false); break;
        case MODIFY:
          switch(instructions[opcode & 0xFF]) {
            case ASL: setCIf((operand & 0x80) != 0); operand <<= 1; setNZ(operand); break;
            case ROL: int b0 = carry() ? 1 : 0; setCIf((operand & 0x80) != 0); operand <<= 1; operand |= b0; setNZ(operand); break;
            case LSR: setCIf((operand & 0x01) != 0); operand >>= 1; setNZ(operand); break;
            case ROR: int b7 = carry() ? 0x80 : 0; setCIf((operand & 0x01) != 0); operand >>= 1; operand |= b7; setNZ(operand); break;
            case INC: operand++; setNZ(operand); break;
            case DEC: operand--; setNZ(operand); break;
            case RMB: operand &= ~(1 << ((opcode & 0b01110000) >> 4)); break;
            case SMB: operand |=  (1 << ((opcode & 0b01110000) >> 4)); break;
            default:
          }
          break;
        case R_FFFC: address.value(0xFFFC); rwb.value(true); break;
        case R_FFFD: address.value(0xFFFD); rwb.value(true); break;
        case R_PC_AD_SYNC:
          pc = (short)((adh << 8) | (adl & 0xFF));
          // intentional fallthrough
        case R_PC_SYNC:
          switch(instructions[opcode & 0xFF]) {
             case STP: stopped = true; break;
             default:
          }
          address.value(pc);
          rwb.value(true);
          sync.value(true);
          resetting = !resb.value();
          halfstep = -1; // will be incremented to 0 at end of loop.
          break;
        case R_PC_INC_SYNC:
          switch(instructions[opcode & 0xFF]) {
            case CLC: p &= ~CARRY; break;
            case SEC: p |=  CARRY; break;
            case CLD: p &= ~DECIMAL; break;
            case SED: p |=  DECIMAL; break;
            case CLI: p &= ~INTERRUPT_DISABLE; break;
            case SEI: p |=  INTERRUPT_DISABLE; break;
            case CLV: p &= ~OVERFLOW; break;
            case DEX: setNZ(--x); break;
            case INX: setNZ(++x); break;
            case DEY: setNZ(--y); break;
            case INY: setNZ(++y); break;
            case TAX: x = a; setNZ(x); break;
            case TXA: a = x; setNZ(a); break;
            case TAY: y = a; setNZ(y); break;
            case TYA: a = y; setNZ(a); break;
            case TSX: x = s; setNZ(x); break;
            case TXS: s = x;           break;
            case LDA: a  = operand; setNZ(a); break;
            case LDX: x  = operand; setNZ(x); break;
            case LDY: y  = operand; setNZ(y); break;
            case AND: a &= operand; setNZ(a); break;
            case ORA: a |= operand; setNZ(a); break;
            case EOR: a ^= operand; setNZ(a); break;
            case CMP: setNZ((byte)(a - operand)); setCIf(a >= operand); break;
            case CPX: setNZ((byte)(x - operand)); setCIf(x >= operand); break;
            case CPY: setNZ((byte)(y - operand)); setCIf(y >= operand); break;
            case BIT:
              p = (byte)((a & operand) == 0 ? p | ZERO : p & ~ZERO);
              p = (byte)((operand & 0x40) != 0 ? p | OVERFLOW : p & ~OVERFLOW);
              p = (byte)((operand & 0x80) != 0 ? p | NEGATIVE : p & ~NEGATIVE);
              break;
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
            case ASL:
              if(addressingModes[opcode] == IMPLIED) {
                setCIf((a & 0x80) != 0);
                a <<= 1;
                setNZ(a);
              }
              break;
            case ROL:
              if(addressingModes[opcode] == IMPLIED) {
                int b0 = carry() ? 1 : 0;
                setCIf((a & 0x80) != 0);
                a <<= 1;
                a |= b0;
                setNZ(a);
              }
              break;
            case LSR:
              if(addressingModes[opcode] == IMPLIED) {
                setCIf((a & 0x01) != 0);
                a >>= 1;
                setNZ(a);
              }
              break;
            case ROR:
              if(addressingModes[opcode] == IMPLIED) {
                int b7 = carry() ? 0x80 : 0;
                setCIf((a & 0x01) != 0);
                a >>= 1;
                a |= b7;
                setNZ(a);
              }
              break;
            case INC: if(addressingModes[opcode] == IMPLIED) { setNZ(++a); } break;
            case DEC: if(addressingModes[opcode] == IMPLIED) { setNZ(--a); } break;

            case STP: stopped = true; break;
            default:
          }
          address.value(++pc);
          rwb.value(true);
          sync.value(true);
          resetting = !resb.value();
          halfstep = -1; // will be incremented to 0 at end of loop.
          break;
        case NOOP: break;
      }
      halfstep++;
    }
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
