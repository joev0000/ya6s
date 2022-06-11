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

        LDA     #$20            ; Transmitter Holding Register bit
        LDX     #$00            ; Start at the first message byte.
NEXT:   LDY     MSG,X           ; Load the message byte.
        BEQ     DONE            ; If it's zero, we're done
WAIT:   BIT     U_LSR           ; Check to see if the transmitter is ready
        BEQ     WAIT            ; If not, try again.
        STY     U_THR           ; Write the message byte
        INX                     ; Increment to the next byte index.
        BRA     NEXT            ; Move on the the next byte.

DONE:   STP                     ; Halt the processor.

NMI:
IRQ:    RTI

MSG:    .BYTE   "Hello, world!", $0D, $0A, $00

        .SEGMENT "VECTOR"
V_NMI:  .WORD   NMI
V_RESET:.WORD   START
V_IRQ:  .WORD   IRQ
