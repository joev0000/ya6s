; Copyright (C) 2021, 2022 Joseph Vigneau

        .IMPORT BUFINI
        .IMPORT BUFRD
        .IMPORT BPUTS

        .SEGMENT "CODE"
        .PC02

UART    :=      $F000           ; UART base address
U_RBR   :=      UART + 0        ; Receiver Buffer Register (read)
U_THR   :=      UART + 0        ; Transmitter Holding Register (write)
U_DLL   :=      UART + 0        ; Divisor Latch, Least Significant (DLAB=1)
U_DLM   :=      UART + 1        ; Divisor Latch, Most Significant (DLAB=1)
U_IER   :=      UART + 1        ; Interrupt Enable Register
U_IIR   :=      UART + 2        ; Interrupt Identification Register (read)
U_LCR   :=      UART + 3        ; Line Control Register
U_LSR   :=      UART + 5        ; Line Status Register

COUNTR  :=      $F100           ; Counter base address
C_LO    :=      COUNTR + 0      ; Low bits of counter
C_MID   :=      COUNTR + 1      ; Middle bits of counter
C_HI    :=      COUNTR + 2      ; High bits of counter
C_CTRL  :=      COUNTR + 3      ; Counter control register

START:  LDX     #$FF            ; Initialize stack.
        TXS
        JSR     U_INIT          ; Initialize UART
        JSR     C_INIT          ; Initialize counter
        JSR     BUFINI          ; Initialize ring buffer

        LDA     C_CTRL          ; A = Counter control
        ORA     #%00000101      ; set Counter Enable and Counter Interrupt bits
        STA     C_CTRL          ; Store counter control

        CLI                     ; Clear interrupt disable flag.

@SPIN:  WAI                     ; Pause until an interrupt occurs.
        BRA     @SPIN           ; Repeat forever.

C_INIT: LDA     C_CTRL          ; A = Counter control
        AND     #%11111010      ; Clear counter and interrupt enable bits
        STA     C_CTRL          ; Store counter control

        LDA     #$40            ; A = Low bits of 1000000(10)
        STA     C_LO            ; Store them in counter low
        LDA     #$42            ; A = Middle bits of 1000000(10)
        STA     C_MID           ; Store them in counter mid
        LDA     #$0F            ; A = High bits of 1000000(10)
        ;LDA     #$00
        STA     C_HI            ; Store them in counter high
        RTS                     ; return

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
; Counter interrupt handler
;
; Writes a message to the ring buffer, and enables write interrupts in UART
;
C_IRQ:
        PHA                     ; Stash A
        PHX                     ; Stash X
        STZ     C_CTRL          ; Disable counter and interrupt enable bits
        LDA     #>TICK          ; Load the hi pointer to the message
        LDX     #<TICK          ; Load the lo pointer to the message
        JSR     BPUTS           ; Write the message to the ring buffer
        LDA     #%00000010      ; Enable Transmitter Holding Register Empty Interrupt
        TSB     U_IER           ; Set the interrupt bit.
        JSR     C_INIT          ; Reinitialize counter
        LDA     #%00000101      ; Counter Enable the Counter Interrupt bits.
        TSB     C_CTRL          ; set the bits on Counter
        PLX                     ; Restore X
        PLA                     ; Restore A
        RTI                     ; Return from interrupt

;
; UART interrupt handler
;
; If the transmitter holding register is empty, read a byte from the ring
; buffer and write it to the transmitter.  If the ring buffer is empty,
; disable the UART write interrupts.
;
U_IRQ:
        PHA                     ; Stash A
        LDA     #%00000110      ; Interrupt type mask
        AND     U_IIR           ; Check the interrupt type
        CMP     #%00000010      ; Is it THRE?
        BNE     @DONE           ; No, we're done here

        JSR     BUFRD           ; Get the next byte from the buffer
        BCS     @EMPTY          ; The buffer is empty, skip ahead.

        STA     U_THR           ; Write the byte to the transmitter.
        BRA     @DONE           ; We're done
@EMPTY: LDA     #%00000010      ; Disable Transmitter Holding Register Empty Interrupt
        TRB     U_IER           ; Reset the interrupt bit


@DONE:  PLA                     ; Restore A
        RTI                     ; Return from interrupt handler.

;
; Overall interrupt handler
;
; Dispatch to Counter or UART interrupt service routine.
;
IRQ:
        PHA                     ; Stash A
        LDA     #%00000001      ; UART Interrupt Pending bit
        BIT     U_IIR           ; Check the Interrupt Pending bit
        BNE     @NOT_U          ; If not set, skip ahead
        PLA                     ; Restore A
        JMP     U_IRQ           ; Go to UART interrupt handler

@NOT_U: PLA                     ; Restore A
        BIT     C_CTRL          ; Check the counter interrupt flag
        BVC     @NOT_C          ; if it's not set, skip ahead
        JMP     C_IRQ           ; Go to the Counter interrupt handler

@NOT_C: RTI                     ; It's something else, just return

;
; Not Maskable Interrupt
;
; No nothing.
;
NMI:    RTI                     ; Return from interrupt handler.

        .SEGMENT "RODATA"
TICK:   .BYTE    "All work and no play makes Jack a dull boy.", $0D, $0A, 0

        .SEGMENT "VECTOR"
V_NMI:  .WORD   NMI
V_RESET:.WORD   START
V_IRQ:  .WORD   IRQ
