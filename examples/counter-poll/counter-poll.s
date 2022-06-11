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

COUNTR  :=      $F100           ; Counter base address
C_LO    :=      COUNTR + 0      ; Low bits of counter
C_MID   :=      COUNTR + 1      ; Middle bits of counter
C_HI    :=      COUNTR + 2      ; High bits of counter
C_CTRL  :=      COUNTR + 3      ; Counter control register

START:  JSR     U_INIT          ; Initialize the UART
        JSR     C_INIT          ; Initialize the Counter

        LDA     C_CTRL          ; Get the Counter control register
        ORA     #$01            ; Set the enable bit
        STA     C_CTRL          ; Save it to the control register

LOOP:   BIT     C_CTRL          ; Check the counter control bits
        BVC     LOOP            ; If bit 6 (counter zero) not set, loop.

        LDA     #>TICK          ; Load the hi pointer to the message
        LDX     #<TICK          ; Load the lo pointer to the message
        JSR     U_PUTS          ; Print the message
        BRA     START           ; Start over

;
; Initialize the Counter device
;
C_INIT: LDA     C_CTRL          ; A = Counter control
        AND     #$FA            ; Clear counter and interrupt enable bits
        STA     C_CTRL          ; Store counter control

        LDA     #$40            ; A = Low bits of 1000000(10)
        STA     C_LO            ; Store them in counter low
        LDA     #$42            ; A = Middle bits of 1000000(10)
        STA     C_MID           ; Store them in counter mid
        LDA     #$0F            ; A = High bits of 1000000(10)
        STA     C_HI            ; Store them in counter high
        RTS                     ; return

;
; Initialize the UART device.
;
U_INIT: LDA     #$83            ; A = Set Divisor Latch Access Bit, 8-N-1
        STA     U_LCR           ; Line Control Register = A
                                ; Set 9600 bps
        LDA     #$78            ; A = Divisor Latch Low bits
        STA     U_DLL           ; Divisor Latch Least Significant Bits = A
        STZ     U_DLM           ; Divisor Latch Most Significant Bits = 0
                                ; To use 110 bps
                                ;   LDA #$E9
                                ;   STA U_DLL
                                ;   LDA #$28
                                ;   STA U_DLM
        LDA     #$03            ; A = Clear Divisor Latch Access Bit, 8-N-1
        STA     U_LCR           ; Line Control Register = A
        RTS                     ; return

;
; Write a string pointed to by AX to the UART.
;
U_PUTS: PHY                     ; Stash the Y register
        LDY     $00             ; Load the value at $00
        PHY                     ; Stash it
        LDY     $01             ; Load the value at $01
        PHY                     ; Stash it
        PHA                     ; Stash the A register

        STX     $00             ; Store the lo byte of the string to $00
        STA     $01             ; Store the hi byte of the string to $01
        LDY     #$00            ; Set the byte index to zero.
U_NEXT: LDA     ($00),Y         ; Load the next byte of the string
        BEQ     RET             ; If it's zero, clean up and return
        TAX                     ; Put the byte into X
        LDA     #$20            ; Set the bitmask to check if the
                                ;   Transmit Register is empty
U_WAIT: BIT     U_LSR           ; Check the Transmit Register Empty bit
        BEQ     U_WAIT          ; If not set, try again
        STX     U_THR           ; Put the byte into the Transmit Register
        INY                     ; Increment the index
        BRA     U_NEXT          ; Go back for the next byte

RET:    PLA                     ; Restore A
        PLY                     ; Get the original value of $01 from the stack
        STY     $01             ; Store it back to $01
        PLY                     ; Get the original value of $00 from the stack
        STY     $00             ; Store it back in $00
        PLY                     ; Restore Y
        RTS                     ; Return to caller

;
; Interrupt handler
;
NMI:
IRQ:    RTI                     ; Return from interrupt.

TICK:   .BYTE    "All work and no play makes Jack a dull boy.", $0D, $0A, 0

        .SEGMENT "VECTOR"
V_NMI:  .WORD   NMI
V_RESET:.WORD   START
V_IRQ:  .WORD   IRQ
