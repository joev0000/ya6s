/* Copyright (C) 2021, 2022 Joseph Vigneau */

package joev.ya6s;

/**
 * An enumeration of all instructions.
 */
public enum Instruction {
  // Illegal instruction
  XXX,

  // 6502 instructions
  BRK, NOP,
  ADC, SBC, AND, ORA, EOR, BIT,
  LDA, LDX, LDY, STA, STX, STY,
  CMP, CPX, CPY,
  ASL, LSR, ROL, ROR,
  DEC, DEX, DEY, INC, INX, INY,
  BCC, BCS, BEQ, BNE, BMI, BPL, BVC, BVS,
  CLC, CLD, CLI, CLV, SEC, SED, SEI,
  JMP, JSR,
  PHA, PHP, PLA, PLP,
  RTI, RTS,
  TAX, TAY, TSX, TXA, TXS, TYA,

  // Rockwell 65C02 instructions (implemented by WDC)
  RMB, SMB, BBR, BBS,

  // WDC 65C02 instructions
  WAI, STP,
  STZ,
  BRA,
  PHX, PHY, PLX, PLY,
  TRB, TSB
}

