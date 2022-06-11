; Copyright (C) 2021, 2022 Joseph Vigneau

        .PC02
        .SEGMENT "CODE"

UART    :=      $F000           ; UART base address
U_RBR   :=      UART + 0        ; Receiver Buffer Register
U_THR   :=      UART + 0        ; Transmitter Holding Register
U_DLL   :=      UART + 0        ; Divisor Latch, Least Significant
U_DLM   :=      UART + 1        ; Diviror Latch, Most Significant
U_LCR   :=      UART + 3        ; Line Control Register
U_LSR   :=      UART + 5        ; Line Status Register

START:  LDA     #$83            ; A = Set Divisor Latch Access Bit, 8-N-1
        STA     U_LCR           ; Line Control Register = A
        LDA     #$78            ; A = Divisor Latch Low bits
        STA     U_DLL           ; Divisor Latch Least Significant Bits = A
        STZ     U_DLM           ; Divisor Latch Most Significant Bits = 0
        LDA     #$03            ; A = Clear Divisor Latch Access Bit, 8-N-1
        STA     U_LCR           ; Line Control Register = A

LOOP:   LDA     #$01            ; Set Data Ready bit
RLOOP:  BIT     U_LSR           ; Check Line Status Register
        BEQ     RLOOP           ; If not set, try again.
        LDX     U_RBR           ; X = Receive Buffer Register
        LDA     #$20            ; Set the Transmitter Holding Register Empty bit.
WLOOP:  BIT     U_LSR           ; Check Line Status Register
        BEQ     WLOOP           ; If not set, try again.
        STX     U_THR           ; Transmitter Holding Register = X
        BRA     LOOP            ; Read the next byte

NMI:
IRQ:    RTI

        .SEGMENT "VECTOR"
V_NMI:  .WORD   NMI
V_RESET:.WORD   START
V_IRQ:  .WORD   IRQ
