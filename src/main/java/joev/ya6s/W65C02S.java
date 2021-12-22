package joev.ya6s;

import static joev.ya6s.AddressingMode.*;
import static joev.ya6s.Instruction.*;
import static joev.ya6s.Register.*;

import joev.ya6s.signals.Bus;
import joev.ya6s.signals.Signal;

public class W65C02S {
  private static final Instruction[] instructions = {
  // x0  x1  x2  x3  x4  x5  x6  x7   x8  x9  xA  xB  xC  xD  xE  xF
    BRK,ORA,XXX,XXX,TSB,ORA,ASL,RMB, PHP,ORA,ASL,XXX,TSB,ORA,ASL,BBR, // 0x
    BPL,ORA,ORA,XXX,TRB,ORA,ASL,RMB, CLC,ORA,INC,XXX,TRB,ORA,ASL,BBR, // 1x
    JSR,AND,XXX,XXX,BIT,AND,ROL,RMB, PLP,AND,ROL,XXX,BIT,AND,ROL,BBR, // 2x
    BMI,AND,AND,XXX,BIT,AND,ROL,RMB, SEC,AND,DEC,XXX,BIT,AND,ROL,BBR, // 3x

    RTI,EOR,XXX,XXX,XXX,EOR,LSR,RMB, PHA,EOR,LSR,XXX,JMP,EOR,LSR,BBR, // 4x
    BVC,EOR,EOR,XXX,XXX,EOR,LSR,RMB, CLI,EOR,PHY,XXX,XXX,EOR,LSR,BBR, // 5x
    RTS,ADC,XXX,XXX,STZ,ADC,ROR,RMB, PLA,ADC,ROR,XXX,JMP,ADC,ROR,BBR, // 6x
    BVS,ADC,ADC,XXX,STZ,ADC,ROR,RMB, SEI,ADC,PLY,XXX,JMP,ADC,ROR,BBR, // 7x

    BRA,STA,XXX,XXX,STY,STA,STX,SMB, DEY,BIT,TXA,XXX,STY,STA,STX,BBS, // 8x
    BCC,STA,STA,XXX,STY,STA,STX,SMB, TYA,STA,TXS,XXX,STZ,STA,STZ,BBS, // 9x
    LDY,LDA,LDX,XXX,LDY,LDA,LDX,SMB, TAY,LDA,TAX,XXX,LDY,LDA,LDX,BBS, // Ax
    BCS,LDA,LDA,XXX,LDY,LDA,LDX,SMB, CLV,LDA,TSX,XXX,LDY,LDA,LDX,BBS, // Bx

    CPY,CMP,XXX,XXX,CPY,CMP,DEC,SMB, INY,CMP,DEX,WAI,CPY,CMP,DEC,BBS, // Cx
    BNE,CMP,CMP,XXX,XXX,CMP,DEC,SMB, CLD,CMP,PHX,STP,XXX,CMP,DEC,BBS, // Dx
    CPX,SBC,XXX,XXX,CPX,SBC,INC,SMB, INX,SBC,NOP,XXX,CPX,SBC,INC,BBS, // Ex
    BEQ,SBC,SBC,XXX,XXX,SBC,INC,SMB, SED,SBC,PLX,XXX,XXX,SBC,INC,BBS  // Fx
  };

  private static final AddressingMode[] addressingModes = {
    STACK_BRK,              // 00 BRK, STACK
    ZERO_PAGE_INDIRECT_X,   // 01 ORA (zp,X)
    NOP_2BYTE_2CYCLE,       // 02 XXX (undefined, 2 byte, 2 cycle)
    NOP_1BYTE_1CYCLE,       // 03 XXX (undefined, 1 byte, 1 cycle)
    ZERO_PAGE_RMW,          // 04 TSB, ZERO_PAGE
    ZERO_PAGE,              // 05 ORA zp
    ZERO_PAGE_RMW,          // 06 ASL zp
    ZERO_PAGE_RMW,          // 07 RMB0
    STACK_PUSH,             // 08 PHP
    IMMEDIATE,              // 09 ORA #
    ACCUMULATOR,            // 0A ASL
    NOP_1BYTE_1CYCLE,       // 0B XXX (undefined, 1 byte, 1 cycle)
    ABSOLUTE_RMW,           // 0C TSB, ABSOLUTE_RMW
    ABSOLUTE,               // 0D ORA abs
    ABSOLUTE_RMW,           // 0E ASL abs
    RELATIVE_BB,            // 0F BBR0

    RELATIVE,               // 10 BPL rel
    ZERO_PAGE_INDEXED,      // 11 ORA (zp),Y
    ZERO_PAGE_INDIRECT,     // 12 ORA (zp)
    NOP_1BYTE_1CYCLE,       // 13 XXX (undefined, 1 byte, 1 cycle)
    ZERO_PAGE_RMW,          // 14 TRB, ZERO_PAGE
    ZERO_PAGE_X,            // 15 ORA zp,X
    ZERO_PAGE_X_RMW,        // 16 ASL zp,X
    ZERO_PAGE_RMW,          // 17 RMB1
    IMPLIED,                // 18 CLC
    ABSOLUTE_Y,             // 19 ORA abs,Y
    ACCUMULATOR,            // 1A INC
    NOP_1BYTE_1CYCLE,       // 1B XXX (undefined, 1 byte, 1 cycle)
    ABSOLUTE_RMW,           // 1C TRB, ABSOLUTE_RMW
    ABSOLUTE_X,             // 1D ORA abs,X
    ABSOLUTE_X_RMW,         // 1E ASL abs,X
    RELATIVE_BB,            // 1F BBR1

    ABSOLUTE_JSR,           // 20 JSR abs
    ZERO_PAGE_INDIRECT_X,   // 21 AND (zp,X)
    NOP_2BYTE_2CYCLE,       // 22 XXX (undefined, 2 byte, 2 cycle)
    NOP_1BYTE_1CYCLE,       // 23 XXX (undefined, 1 byte, 1 cycle)
    ZERO_PAGE,              // 24 BIT zp
    ZERO_PAGE,              // 25 AND zp
    ZERO_PAGE_RMW,          // 26 ROL zp
    ZERO_PAGE_RMW,          // 27 RMB2
    STACK_PULL,             // 28 PLP
    IMMEDIATE,              // 29 AND #
    ACCUMULATOR,            // 2A ROL
    NOP_1BYTE_1CYCLE,       // 2B XXX (undefined, 1 byte, 1 cycle)
    ABSOLUTE,               // 2C BIT abs
    ABSOLUTE,               // 2D AND abs
    ABSOLUTE_RMW,           // 2E ROL abs
    RELATIVE_BB,            // 2F BBR2

    RELATIVE,               // 30 BMI
    ZERO_PAGE_INDEXED,      // 31 AND (zp),Y
    ZERO_PAGE_INDIRECT,     // 32 AND (zp)
    NOP_1BYTE_1CYCLE,       // 33 XXX (undefined, 1 byte, 1 cycle)
    ZERO_PAGE_X,            // 34 BIT zp,X
    ZERO_PAGE_X,            // 35 AND zp,X
    ZERO_PAGE_X_RMW,        // 36 ROL zp,X
    ZERO_PAGE_RMW,          // 37 RMB3
    IMPLIED,                // 38 SEC
    ABSOLUTE_Y,             // 39 AND abs,Y
    ACCUMULATOR,            // 3A DEC
    NOP_1BYTE_1CYCLE,       // 3B XXX (undefined, 1 byte, 1 cycle)
    ABSOLUTE_X,             // 3C BIT abs,X
    ABSOLUTE_X,             // 3D AND abs,X
    ABSOLUTE_X_RMW,         // 3E ROL abs,X
    RELATIVE_BB,            // 3F BBR3

    STACK_RTI,              // 40 RTI, STACK
    ZERO_PAGE_INDIRECT_X,   // 41 EOR (zp,X)
    NOP_2BYTE_2CYCLE,       // 42 XXX (undefined, 2 byte, 2 cycle)
    NOP_1BYTE_1CYCLE,       // 43 XXX (undefined, 1 byte, 1 cycle)
    NOP_2BYTE_3CYCLE,       // 44 XXX (undefined, 2 byte, 3 cycle)
    ZERO_PAGE,              // 45 EOR zp
    ZERO_PAGE_RMW,          // 46 LSR zp
    ZERO_PAGE_RMW,          // 47 RMB4
    STACK_PUSH,             // 48 PHA
    IMMEDIATE,              // 49 EOR #
    ACCUMULATOR,            // 4A LSR
    NOP_1BYTE_1CYCLE,       // 4B XXX (undefined, 1 byte, 1 cycle)
    ABSOLUTE_JMP,           // 4C JMP abs
    ABSOLUTE,               // 4D EOR abs
    ABSOLUTE_RMW,           // 4E LSR abs
    RELATIVE_BB,            // 4F BBR4

    RELATIVE,               // 50 BVC rel
    ZERO_PAGE_INDEXED,      // 51 EOR (zp),Y
    ZERO_PAGE_INDIRECT,     // 52 EOR (zp)
    NOP_1BYTE_1CYCLE,       // 53 XXX (undefined, 1 byte, 1 cycle)
    NOP_2BYTE_4CYCLE,       // 54 XXX (undefined, 2 byte, 4 cycle)
    ZERO_PAGE_X,            // 55 EOR zp,X
    ZERO_PAGE_X_RMW,        // 56 LSR zp,X
    ZERO_PAGE_RMW,          // 57 RMB5
    IMPLIED,                // 58 CLI
    ABSOLUTE_Y,             // 59 EOR abs,Y
    STACK_PUSH,             // 5A PHY
    NOP_1BYTE_1CYCLE,       // 5B XXX (undefined, 1 byte, 1 cycle)
    NOP_3BYTE_8CYCLE,       // 5C XXX (undefined, 3 byte, 8 cycle)
    ABSOLUTE_X,             // 5D EOR abs,X
    ABSOLUTE_X_RMW,         // 5E LSR abs,X
    RELATIVE_BB,            // 5F BBR5

    STACK_RTS,              // 60 RTS
    ZERO_PAGE_INDIRECT_X,   // 61 ADC (zp,X)
    NOP_2BYTE_2CYCLE,       // 62 XXX (undefined, 2 byte, 2 cycle)
    NOP_1BYTE_1CYCLE,       // 63 XXX (undefined, 1 byte, 1 cycle)
    ZERO_PAGE_W,            // 64 STZ zp
    ZERO_PAGE,              // 65 ADC zp
    ZERO_PAGE_RMW,          // 66 ROR zp
    ZERO_PAGE_RMW,          // 67 RMB6
    STACK_PULL,             // 68 PLA
    IMMEDIATE,              // 69 ADC #
    ACCUMULATOR,            // 6A ROR
    NOP_1BYTE_1CYCLE,       // 6B XXX (undefined, 1 byte, 1 cycle)
    ABSOLUTE_INDIRECT,      // 6C JMP
    ABSOLUTE,               // 6D ADC abs
    ABSOLUTE_RMW,           // 6E ROR abs
    RELATIVE_BB,            // 6F BBR6

    RELATIVE,               // 70 BVS rel
    ZERO_PAGE_INDEXED,      // 71 ADC (zp),Y
    ZERO_PAGE_INDIRECT,     // 72 ADC (zp)
    NOP_1BYTE_1CYCLE,       // 73 XXX (undefined, 1 byte, 1 cycle)
    ZERO_PAGE_X_W,          // 74 STZ zp,X
    ZERO_PAGE_X,            // 75 ADC zp,X
    ZERO_PAGE_X_RMW,        // 76 ROR zp,X
    ZERO_PAGE_RMW,          // 77 RMB7
    IMPLIED,                // 78 SEI
    ABSOLUTE_Y,             // 79 ADC abs,Y
    STACK_PULL,             // 7A PLY
    NOP_1BYTE_1CYCLE,       // 7B XXX (undefined, 1 byte, 1 cycle)
    ABSOLUTE_INDEXED_INDIRECT, // 7C JMP
    ABSOLUTE_X,             // 7D ADC abs,X
    ABSOLUTE_X_RMW,         // 7E ROR abs,X
    RELATIVE_BB,            // 7F BBR7

    RELATIVE,               // 80 BRA rel
    ZERO_PAGE_INDIRECT_X_W, // 81 STA (zp,X)
    NOP_2BYTE_2CYCLE,       // 82 XXX (undefined, 2 byte, 2 cycle)
    NOP_1BYTE_1CYCLE,       // 83 XXX (undefined, 1 byte, 1 cycle)
    ZERO_PAGE_W,            // 84 STY zp
    ZERO_PAGE_W,            // 85 STA zp
    ZERO_PAGE_W,            // 86 STX zp
    ZERO_PAGE_RMW,          // 87 SMB0
    IMPLIED,                // 88 DEY
    IMMEDIATE,              // 89 BIT #
    IMPLIED,                // 8A TXA
    NOP_1BYTE_1CYCLE,       // 8B XXX (undefined, 1 byte, 1 cycle)
    ABSOLUTE_W,             // 8C STY abs
    ABSOLUTE_W,             // 8D STA abs
    ABSOLUTE_W,             // 8E STX abs
    RELATIVE_BB,            // 8F BBS0

    RELATIVE,               // 90 BCC rel
    ZERO_PAGE_INDEXED_W,    // 91 STA (zp),Y
    ZERO_PAGE_INDIRECT_W,   // 92 STA (zp)
    NOP_1BYTE_1CYCLE,       // 93 XXX (undefined, 1 byte, 1 cycle)
    ZERO_PAGE_X_W,          // 94 STY zp,X
    ZERO_PAGE_X_W,          // 95 STA zp,X
    ZERO_PAGE_Y_W,          // 96 STX zp,Y
    ZERO_PAGE_RMW,          // 97 SMB1
    IMPLIED,                // 98 TYA
    ABSOLUTE_Y_W,           // 99 STA abs,Y
    IMPLIED,                // 9A TXS
    NOP_1BYTE_1CYCLE,       // 9B XXX (undefined, 1 byte, 1 cycle)
    ABSOLUTE_W,             // 9C STZ abs
    ABSOLUTE_X_W,           // 9D STA abs,X
    ABSOLUTE_X_W,           // 9E STZ abs,X
    RELATIVE_BB,            // 9F BBS1

    IMMEDIATE,              // A0 LDY #
    ZERO_PAGE_INDIRECT_X,   // A1 LDA (zp,X)
    IMMEDIATE,              // A2 LDX #
    NOP_1BYTE_1CYCLE,       // A3 XXX (undefined, 1 byte, 1 cycle)
    ZERO_PAGE,              // A4 LDY zp
    ZERO_PAGE,              // A5 LDA zp
    ZERO_PAGE,              // A6 LDX zp
    ZERO_PAGE_RMW,          // A7 SMB2
    IMPLIED,                // A8 TAY
    IMMEDIATE,              // A9 LDA #
    IMPLIED,                // AA TAX
    NOP_1BYTE_1CYCLE,       // AB XXX (undefined, 1 byte, 1 cycle)
    ABSOLUTE,               // AC LDY abs
    ABSOLUTE,               // AD LDA abs
    ABSOLUTE,               // AE LDX abs
    RELATIVE_BB,            // AF BBS2

    RELATIVE,               // B0 BCS rel
    ZERO_PAGE_INDEXED,      // B1 LDA (zp),Y
    ZERO_PAGE_INDIRECT,     // B2 LDA (zp)
    NOP_1BYTE_1CYCLE,       // B3 XXX (undefined, 1 byte, 1 cycle)
    ZERO_PAGE_X,            // B4 LDY zp,X
    ZERO_PAGE_X,            // B5 LDA zp,X
    ZERO_PAGE_Y,            // B6 LDX zp,Y
    ZERO_PAGE_RMW,          // B7 SMB3
    IMPLIED,                // B8 CLV
    ABSOLUTE_Y,             // B9 LDA abs,Y
    IMPLIED,                // BA TSX
    NOP_1BYTE_1CYCLE,       // BB XXX (undefined, 1 byte, 1 cycle)
    ABSOLUTE_X,             // BC LDY abs,X
    ABSOLUTE_X,             // BD LDA abs,X
    ABSOLUTE_Y,             // BE LDX abs,Y
    RELATIVE_BB,            // BF BBS3

    IMMEDIATE,              // C0 CPY #
    ZERO_PAGE_INDIRECT_X,   // C1 CMP (zp,X)
    NOP_2BYTE_2CYCLE,       // C2 XXX (undefined, 2 byte, 2 cycle)
    NOP_1BYTE_1CYCLE,       // C3 XXX (undefined, 1 byte, 1 cycle)
    ZERO_PAGE,              // C4 CPY zp
    ZERO_PAGE,              // C5 CMP zp
    ZERO_PAGE_RMW,          // C6 DEC zp
    ZERO_PAGE_RMW,          // C7 SMB4
    IMPLIED,                // C8 INY
    IMMEDIATE,              // C9 CMP #
    IMPLIED,                // CA DEX
    NOT_IMPLEMENTED,        // CB WAI
    ABSOLUTE,               // CC CPY abs
    ABSOLUTE,               // CD CMP abs
    ABSOLUTE_RMW,           // CE DEC
    RELATIVE_BB,            // CF BBS4

    RELATIVE,               // D0 BNE rel
    ZERO_PAGE_INDEXED,      // D1 CMP (zp,Y)
    ZERO_PAGE_INDIRECT,     // D2 CMP (zp)
    NOP_1BYTE_1CYCLE,       // D3 XXX (undefined, 1 byte, 1 cycle)
    NOP_2BYTE_4CYCLE,       // D4 XXX (undefined, 2 byte, 4 cycle)
    ZERO_PAGE_X,            // D5 CMP zp,X
    ZERO_PAGE_X_RMW,        // D6 DEC zp,X
    ZERO_PAGE_RMW,          // D7 SMB5
    IMPLIED,                // D8 CLD
    ABSOLUTE_Y,             // D9 CMP abs,Y
    STACK_PUSH,             // DA PHX
    IMPLIED,                // DB STP
    NOP_3BYTE_4CYCLE,       // DC XXX (undefined, 3 byte, 4 cycle)
    ABSOLUTE_X,             // DD CMP abs,X
    ABSOLUTE_X_RMW,         // DE DEC abs,X
    RELATIVE_BB,            // DF BBS5

    IMMEDIATE,              // E0 CPX #
    ZERO_PAGE_INDIRECT_X,   // E1 SBC (zp,X)
    NOP_2BYTE_2CYCLE,       // E2 (undefined, 2 byte, 2 cycle)
    NOP_1BYTE_1CYCLE,       // E3 XXX (undefined, 1 byte, 1 cycle)
    ZERO_PAGE,              // E4 CPX zp
    ZERO_PAGE,              // E5 SBC zp
    ZERO_PAGE_RMW,          // E6 INC zp
    ZERO_PAGE_RMW,          // E7 SMB6
    IMPLIED,                // E8 INX
    IMMEDIATE,              // E9 SBC #
    IMPLIED,                // EA NOP
    NOP_1BYTE_1CYCLE,       // EB XXX (undefined, 1 byte, 1 cycle)
    ABSOLUTE,               // EC CPX abs
    ABSOLUTE,               // ED SBC abs
    ABSOLUTE_RMW,           // EE INC abs
    RELATIVE_BB,            // EF BBS6

    RELATIVE,               // F0 BEQ rel
    ZERO_PAGE_INDEXED,      // F1 SBC (zp),Y
    ZERO_PAGE_INDIRECT,     // F2 SBC (zp)
    NOP_1BYTE_1CYCLE,       // F3 XXX (undefined, 1 byte, 1 cycle)
    NOP_2BYTE_4CYCLE,       // F4 XXX (undefined, 2 byte, 4 cycle)
    ZERO_PAGE_X,            // F5 SBC zp,X
    ZERO_PAGE_X_RMW,        // F6 INC zp,X
    ZERO_PAGE_RMW,          // F7 SMB7
    IMPLIED,                // F8 SED
    ABSOLUTE_Y,             // F9 SBC abs,Y
    STACK_PULL,             // FA PLX
    NOP_1BYTE_1CYCLE,       // FB XXX (undefined, 1 byte, 1 cycle)
    NOP_3BYTE_4CYCLE,       // FC XXX (undefined, 3 byte, 4 cycle)
    ABSOLUTE_X,             // FD SBC abs,X
    ABSOLUTE_X_RMW,         // FE INC abs,X
    RELATIVE_BB             // FF BBS7
  };

  private enum InterruptMode {
    NONE((short)0),
    NMI((short)0xFFFA),
    RESET((short)0xFFFC),
    IRQ((short)0xFFFE);

    private final short vector;
    InterruptMode(short vector) {
      this.vector = vector;
    }
    public short vector() { return vector; }
  }

  private final byte NEGATIVE          = (byte)0b10000000;
  private final byte OVERFLOW          = (byte)0b01000000;
  private final byte RESERVED          = (byte)0b00100000;
  private final byte BREAK             = (byte)0b00010000;
  private final byte DECIMAL           = (byte)0b00001000;
  private final byte INTERRUPT_DISABLE = (byte)0b00000100;
  private final byte ZERO              = (byte)0b00000010;
  private final byte CARRY             = (byte)0b00000001;

  private short pc;
  private byte a;
  private byte x;
  private byte y;
  private byte s;
  private byte p = RESERVED;

  private byte op;
  private short aa;
  private byte zp;
  private short new_pc;
  private Register readRegister = NULL;
  private InterruptMode interruptMode = InterruptMode.NONE;

  private Cycle[] cycles = IMPLIED.cycles();
  private int cycle;
  private boolean stopped = false;
  private int extraCycles = 0;
  private boolean branch = false;
  private long cycleCount = 0;

  private final Signal phi2;
  private final Signal vpb;
  private final Signal mlb;
  private final Signal sync;
  private final Signal rwb;
  private final Signal rdy;
  private final Signal resb;
  private final Signal irqb;
  private final Signal be;
  private final Bus addressBus;
  private final Bus dataBus;

  private final Signal.Listener tickFn = this::tick;

  public W65C02S(Backplane backplane) {
    this.phi2 = backplane.clock();
    this.vpb = backplane.vpb();
    this.mlb = backplane.mlb();
    this.sync = backplane.sync();
    this.rwb = backplane.rwb();
    this.rdy = new Signal("rdy");
    this.resb = new Signal("resb");
    this.be = backplane.be();
    this.irqb = backplane.irqb();
    this.addressBus = backplane.address();
    this.dataBus = backplane.data();

    rdy.value(true);
    resb.value(true);

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

  public short pc() { return pc; }
  public byte a() { return a; }
  public byte x() { return x; }
  public byte y() { return y; }
  public byte s() { return s; }
  public byte p() { return p; }

  /**
   * Return the processor status bits as a String.
   *
   * @return the status string
   */
  public String status() {
    char[] c = new char[8];
    c[0] = ((p & NEGATIVE) == 0) ?          'n' : 'N';
    c[1] = ((p & OVERFLOW) == 0) ?          'v' : 'V';
    c[2] = '1';
    c[3] = ((p & BREAK) == 0) ?             'b' : 'B';
    c[4] = ((p & DECIMAL) == 0) ?           'd' : 'D';
    c[5] = ((p & INTERRUPT_DISABLE) == 0) ? 'i' : 'I';
    c[6] = ((p & ZERO) == 0) ?              'z' : 'Z';
    c[7] = ((p & CARRY) == 0) ?             'c' : 'C';

    return new String(c);
  }

  public Signal resb() { return resb; }
  public Signal rdy() { return rdy; }

  public boolean stopped() { return stopped; }
  public long cycleCount() { return cycleCount; }

  /**
   * Set or clear the Negative and Zero flags based on the data.
   * If the data is zero, set the Zero flag, otherwise, clear it.
   * Set the Negative flag to the value of thee high bit of the data.
   *
   * @param data the byte used to set or cleart the flags.
   */
  private void setNZ(byte data) {
    p = (byte)((data == 0) ? (p | ZERO)     : (p & ~ZERO));
    p = (byte)((data <  0) ? (p | NEGATIVE) : (p & ~NEGATIVE));
  }

  /**
   * Set the Carry flag to the given value.
   *
   * @param value the new value of the Carry flag.
   */
  private void setC(boolean value) {
    p = (byte)(value ? (p | CARRY) : (p & ~CARRY));
  }

  /**
   * Set the Carry and Overflow flags based on the sum of two bytes
   * and the existing Carry flag.  Used during addition and subtraction.
   *
   * @param a the first byte
   * @param b the second byte
   */
  private void setCV(byte a, byte b) {
    int carry = ((p & CARRY) != 0) ? 1 : 0;
    byte c = (byte)(a + b + carry);
    p = (byte)(((((a & b) | ((a ^ b) & ~c)) & 0x80) != 0) ? (p | CARRY)    : (p & ~CARRY));
    p = (byte)(((((a ^ c) & (b ^ c))        & 0x80) != 0) ? (p | OVERFLOW) : (p & ~OVERFLOW));
  }

  /**
   * Shift the byte left, setting the carry flag from the high bit.
   *
   * @param data the byte to shift.
   */
  private byte doASL(byte data) {
    p = (byte)((data & 0x80) != 0 ? (p | CARRY) : (p & ~CARRY));
    data <<= 1;
    setNZ(data);
    return data;
  }

  /**
   * Rorate the byte left, through the carry flag.
   *
   * @param data the byte to rotate
   */
  private byte doROL(byte data) {
    byte b0 = (byte)((p & CARRY) != 0 ? 1 : 0);
    setC((data & 0x80) != 0);
    data = (byte)((data << 1) | b0);
    setNZ(data);
    return data;
  }

  /**
   * Shift the byte right, setting the high bit to zero, moving the
   * low byte to the carry flag.
   *
   * @param data the byte to shift.
   */
  private byte doLSR(byte data) {
    p = (byte)((data & 0x01) == 0 ? (p & ~CARRY) : (p | CARRY));
    data = (byte)((data >> 1) & 0x7F);
    setNZ(data);
    return data;
  }

  /**
   * ROtate the byte right, through the carry flag.
   *
   * @param data the byte to rotate.
   */
  private byte doROR(byte data) {
    byte b7 = (byte)((p & CARRY) == 0 ? 0 : 0x80);
    p = (byte)((data & 0x01) == 0 ? (p & ~CARRY) : (p | CARRY));
    data = (byte)(((data >> 1) & 0x7F) | b7);
    setNZ(data);
    return data;
  }

  /**
   * Perform a BCD addition.
   *
   * TODO: Set the N and V flags.
   *
   * @param data the byte to add to the accumulator
   * @param c the carry flag value
   */
  private void doADCDecimal(byte data) {
    int c = ((p & CARRY) != 0) ? 1 : 0;
    byte lo = (byte)(((a & 0x0F) + (data & 0x0F) + c) & 0xFF);
    c = 0;
    if(lo > 9) { lo += 6; lo &= 0x0F; c++; }
    byte hi = (byte)((((a >> 4) & 0x0F) + ((data >> 4) & 0x0F) + c) & 0xFF);
    if(hi > 9) { hi += 6; p |= CARRY; } else { p &= ~CARRY; }
    a = (byte)((hi << 4) | lo);
  }

  /**
   * Perform a BCD subtraction..
   *
   * TODO: Set the N and V flags.
   *
   * @param data the byte to add to the accumulator
   */
  private void doSBCDecimal(byte data) {
    int c = ((p & CARRY) != 0) ? 1 : 0;
    byte lo = (byte)(((a & 0x0F) - (data & 0x0F) - 1 + c) & 0xFF);
    c = 0;
    if(lo < 0) { lo += 10; lo &= 0x0F; c++; }
    byte hi = (byte)((((a >> 4) & 0x0F) - ((data >> 4) & 0x0F) - c) & 0xFF);
    if(hi < 0) { hi += 10; p &= ~CARRY; } else { p |= CARRY; }
    a = (byte)((hi << 4) | lo);
  }

  /**
   * Handle a clock edge.
   *
   * @param eventType the type of clock signal edge.
   */
  public void tick(Signal.EventType eventType) {
    // If this is a positive edge, or if the processor is stopped,
    // and we're not resetting, return.
    if(eventType == Signal.EventType.POSITIVE_EDGE || (stopped && resb.value())) {
      return;
    }

    // Increment cyclc counter
    cycleCount++;

    // If we need to inject an extra cycle, return.
    if(extraCycles != 0) {
      sync.value(false);
      extraCycles--;
      return;
    }

    // Set the interrupt mode if we're resetting or interrupted
    if(!resb.value()) {
      cycleCount = 0;
      interruptMode = InterruptMode.RESET;
    }
    else if(((p & INTERRUPT_DISABLE) == 0) && !irqb.value()) {
      interruptMode = InterruptMode.IRQ;
    }

    // If the bus is enabled, latch the data bus value.
    byte data = 0;
    if(be.value()) {
      data = (byte)dataBus.value();
    }

    int opcode = op & 0xFF;

    // Store the data bus value from the previous cycle
    switch(readRegister) {
      case OP:  op = data; opcode = op & 0xFF; break;
      case AAL: aa = (short)((aa & 0xFF00) | (data & 0xFF)); break;
      case AAH: aa = (short)((aa & 0x00FF) | ((data & 0xFF) << 8)); break;
      case DO:  zp = data; break;
      case NEW_PCL: new_pc = (short)((new_pc & 0xFF00) | (data & 0xFF)); break;
      case NEW_PCH: new_pc = (short)((new_pc & 0x00FF) | ((data & 0xFF) << 8)); pc = new_pc; break;
      case P: p = (byte)(data | RESERVED); break;
      case DATA:
        // If we're reading the data for a brancing instruction, make
        // the branching decision here.
        if(addressingModes[opcode] == RELATIVE) {
          byte page = (byte)((pc >> 8) & 0xFF);
          switch(instructions[opcode]) {
            case BPL: if((p & NEGATIVE) == 0) { pc += data; extraCycles++; } break;
            case BMI: if((p & NEGATIVE) != 0) { pc += data; extraCycles++; } break;
            case BVC: if((p & OVERFLOW) == 0) { pc += data; extraCycles++; } break;
            case BVS: if((p & OVERFLOW) != 0) { pc += data; extraCycles++; } break;
            case BCC: if((p & CARRY)    == 0) { pc += data; extraCycles++; } break;
            case BCS: if((p & CARRY)    != 0) { pc += data; extraCycles++; } break;
            case BNE: if((p & ZERO)     == 0) { pc += data; extraCycles++; } break;
            case BEQ: if((p & ZERO)     != 0) { pc += data; extraCycles++; } break;
            case BRA:                         { pc += data; extraCycles++; } break;
            default:
          }

          // Check to see if a page boundary was crossed.
          if(page != (byte)((pc >> 8) & 0xFF)) {
            extraCycles++;
          }
        }
        break;
      case IO: // Internal operation
        switch(instructions[opcode]) {
          case CLC: p &= ~CARRY; break;
          case SEC: p |=  CARRY; break;
          case CLD: p &= ~DECIMAL; break;
          case SED: p |=  DECIMAL; break;
          case CLI: p &= ~INTERRUPT_DISABLE; break;
          case SEI: p |=  INTERRUPT_DISABLE; break;
          case CLV: p &= ~OVERFLOW; break;
          case DEX: x--; setNZ(x); break;
          case DEY: y--; setNZ(y); break;
          case INX: x++; setNZ(x); break;
          case INY: y++; setNZ(y); break;

          case TAX: x = a; setNZ(x); break;
          case TAY: y = a; setNZ(y); break;
          case TXA: a = x; setNZ(a); break;
          case TYA: a = y; setNZ(a); break;
          case TSX: x = s; setNZ(x); break;
          case TXS: s = x; break; // TXS does not set NZ.

          case NOP: case XXX: break;
          case ASL: if(addressingModes[opcode] == ACCUMULATOR) { a = doASL(a); } else { data = doASL(data); } break;
          case ROL: if(addressingModes[opcode] == ACCUMULATOR) { a = doROL(a); } else { data = doROL(data); } break;
          case LSR: if(addressingModes[opcode] == ACCUMULATOR) { a = doLSR(a); } else { data = doLSR(data); } break;
          case ROR: if(addressingModes[opcode] == ACCUMULATOR) { a = doROR(a); } else { data = doROR(data); } break;
          case INC: if(addressingModes[opcode] == ACCUMULATOR) { setNZ(++a); } else { setNZ(++data); } break;
          case DEC: if(addressingModes[opcode] == ACCUMULATOR) { setNZ(--a); } else { setNZ(--data); } break;
          case TRB: p = (byte)(((a & data) == 0) ? p | ZERO : p & ~ZERO); data &= ~a; break;
          case TSB: p = (byte)(((a & data) == 0) ? p | ZERO : p & ~ZERO); data |=  a; break;

          case BBR: branch = (data & (1 << ((op >> 4) & 0x07))) == 0; break;
          case BBS: branch = (data & (1 << ((op >> 4) & 0x07))) != 0; break;

          case RMB: data = (byte)(data & ~(1<<((op >> 4) & 0x07))); break;
          case SMB: data = (byte)(data |  (1<<((op >> 4) & 0x07))); break;

          case RTS: pc++; break;
          case STP: stopped = true; break;
          // case WAI: waiting = true; break;
          case BRK: if(interruptMode == InterruptMode.NONE) interruptMode = InterruptMode.IRQ; break;
          default:
        }
        break;
      default:
    }
    if(cycle == 0) {
      // do the previous ALU op, if necessary..
      switch(instructions[opcode]) {
        case LDA: case PLA: a = data;  setNZ(a); break;
        case LDX: case PLX: x = data;  setNZ(x); break;
        case LDY: case PLY: y = data;  setNZ(y); break;
        case ORA: a |= data; setNZ(a); break;
        case AND: a &= data; setNZ(a); break;
        case EOR: a ^= data; setNZ(a); break;
        case CMP: setNZ((byte)(a - data)); setC((a & 0xFF) >= (data & 0xFF)); break;
        case CPX: setNZ((byte)(x - data)); setC((x & 0xFF) >= (data & 0xFF)); break;
        case CPY: setNZ((byte)(y - data)); setC((y & 0xFF) >= (data & 0xFF)); break;
        case PLP: p = (byte)((data | RESERVED) & ~BREAK); break;
        case BIT:
          p = (byte)((a & data) == 0 ? (p | ZERO) : p & ~ZERO);
          if(addressingModes[opcode] != IMMEDIATE) {
            // BIT Immediate does not modify V or N.
            p = (byte)((data & 0x40) != 0 ? (p | OVERFLOW) : (p & ~OVERFLOW));
            p = (byte)((data & 0x80) != 0 ? (p | NEGATIVE) : (p & ~NEGATIVE));
          }
          break;
        case ADC: {
            if((p & DECIMAL) == 0) {
              byte c = (byte)((p & CARRY) == 0 ? 0 : 1);
              setCV(a, data);
              a += data + c;
            }
            else {
              extraCycles++;
              doADCDecimal(data);
            }
            setNZ(a);
          }
          break;
        case SBC: {
            if((p & DECIMAL) == 0) {
              byte c = (byte)((p & CARRY) == 0 ? 0 : 1);
              setCV(a, (byte)(~data));
              a += (byte)(~data) + c;
            }
            else {
              extraCycles++;
              doSBCDecimal(data);
            }
          }
          setNZ(a);
          break;
        case BBS:
        case BBR:
          // TODO: Add extra cycle on page crossing.
          if(branch) { pc+=data; extraCycles++; } break;
        default:
      }
    }
    else if(cycle == 1) {
      // previous cycle was an opcode read, make sure we have
      // the right cycles for the rest of the instruction.
      cycles = interruptMode == InterruptMode.NONE ? addressingModes[opcode].cycles() : STACK_INTERRUPT.cycles();
      if(interruptMode != InterruptMode.NONE) {
        // Decrement the pc so the correct return address is pushed.
        pc--;
      }
      // System.out.format("tick: PC: %04X op: %s, A: %02X, X: %02X, Y: %02X, S: %02X, P: %02X (%s) c: %d%n", (short)(pc-1), instructions[opcode], a, x, y, s, p, status(), cycleCount);
    }

    Cycle c = cycles[cycle];
    short address = (short)switch(c.address()) {
      case PC_INC -> pc++;
      case PC -> pc;
      case AA -> aa;
      case AA_X -> aa + (x & 0xFF);
      case AA_X_1 -> aa + (x & 0xFF) + 1;
      case AA_INC -> aa++;
      case DO -> zp & 0xFF;
      case DO_INC -> zp++ & 0xFF;
      case DO_X -> (zp + x) & 0xFF;
      case DO_Y -> (zp + y) & 0xFF;
      case DO_X_1 -> (zp + x + 1) & 0xFF;
      case DO_X_INC -> (zp++ + x) & 0xFF;
      case AA_Y -> { if(((aa & 0xFF) + (y & 0xFF)) > 0xFF) { extraCycles++; }; yield aa + (y & 0xFF); }
      case S -> (0x100 | (s & 0xFF));
      case S_INC -> (0x100 | (++s & 0xFF));
      case S_DEC -> (0x100 | (s-- & 0xFF));
      case VAL -> interruptMode.vector();
      case VAH -> { p |= INTERRUPT_DISABLE; p &= ~DECIMAL; yield interruptMode.vector() + 1; }
      default -> addressBus.value();
    };
    data = (byte)switch(c.data()) {
      case DATA -> switch(instructions[opcode]) {
        case STA, PHA -> a;
        case STX, PHX -> x;
        case STY, PHY -> y;
        case STZ -> 0;
        case PHP ->  (byte) (p | BREAK);
        case BRK -> {
          int d = p | BREAK;
          p |= INTERRUPT_DISABLE;
          p &= ~DECIMAL;
          yield d;
        }
        default -> data;
      };
      case PCH -> (pc >> 8) & 0xFF;
      case PCL -> pc & 0xFF;
      case P -> p;
      default -> data;
    };

    if(be.value()) {
      addressBus.value(address);
      if(c.rwb()) {
        readRegister = c.data();
      }
      else {
        readRegister = NULL;
        dataBus.value(data);
      }
    }
    vpb.value(c.vpb());
    mlb.value(c.mlb());
    rwb.value(c.rwb());
    sync.value(c.sync());
    cycle++;
    if(cycle == cycles.length) {
      cycle = 0;
      if(resb.value()) {
        // TODO: This may not need to be done here, but when we check the
        // interrupt status at the start of tick()
        interruptMode = InterruptMode.NONE;
      }
    }
  }
}
