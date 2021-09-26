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

START:  JSR BUFINI
        LDA #$FE
        STA BUFFER+0
        STA BUFFER+1
        JSR BUFRD

        LDA #'Y'
        JSR BUFWR

        LDA #'A'
        JSR BUFWR

        LDA #'6'
        JSR BUFWR

        LDA #'S'
        JSR BUFWR

        JSR BUFRD
        JSR BUFRD
        JSR BUFRD
        JSR BUFRD
        STP

STARTX:  JSR     U_INIT          ; Initialize UART
        JSR     C_INIT          ; Initialize counter

        LDA     C_CTRL          ; A = Counter control
        ORA     #%00000101      ; set Counter Enable and Counter Interrupt bits
        STA     C_CTRL          ; Store counter control

SPIN:   NOP                     ; TODO: This can be WAI
        BRA     SPIN            ; Repeat forever.

C_INIT: LDA     C_CTRL          ; A = Counter control
        AND     #%11111010      ; Clear counter and interrupt enable bits
        STA     C_CTRL          ; Store counter control

        LDA     #$40            ; A = Low bits of 1000000(10)
        STA     C_LO            ; Store them in counter low
        LDA     #$42            ; A = Middle bits of 1000000(10)
        STA     C_MID           ; Store them in counter mid
        LDA     #$0F            ; A = High bits of 1000000(10)
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
        ; TODO: enable interrupts
        RTS                     ; return

BUFINI: PHA                     ; Save A
        LDA     #$02            ; X = 2
        STA     BUFFER+0        ; Set Read Index to 2
        STA     BUFFER+1        ; Set Write Index to 2
        PLA                     ; Restore A
        RTS                     ; Return to caller

; if [0] == [1], the buffer is empty.
; else [0]++, return [0].
BUFRD:  PHX                     ; Save X

        LDX     BUFFER+0        ; X = Read Index
        CPX     BUFFER+1        ; Compare with the Write Index
        BEQ     BUFRD8          ; If same, singal buffer empty

        CPX     #$FF            ; Compere with $#FF
        BNE     BUFRD1          ; If not #$FF, skip ahead.
        LDX     #$01            ; X = 1, which will be incremented next.

BUFRD1: INX                     ; Increment Read Index
        LDA     BUFFER,X        ; Get the byte at the Read Index
        STX     BUFFER+0        ; Store the new Read Index
        CLC                     ; Indicate success
        BRA     BUFRD9          ; Clean up and return

BUFRD8: SEC                     ; Indicate failure

BUFRD9: PLX                     ; Restore X
        RTS                     ; Return to caller

; if [1]+1 == [0], the buffer is full
; else [1]++, write to [1].

; a = [1]+1
; if a = [0] the buffer is full
; else write to [1], [1] = a



BUFWR:  LDX     BUFFER+1        ; X = Write Index
        PHX                     ; Push Write Index
        INX                     ; Increment Write Index
        BNE BUFWR1              ; If not zero, skip ahead
        LDX #$02                ; Wrap around
BUFWR1: PHX                     ; Transfer incremented Write Index
        PLY                     ;   from X to Y
        PLX                     ; Restore original Write Index
        CPY BUFFER+0            ; Compare incremented index with read index
        BEQ BUFWR8 ; full       ; If equal, the buffer is full, skip ahead.
        STA BUFFER,X            ; Store the byte at the write index
        STY BUFFER+1            ; Save the incremented write index
        CLC                     ; Indicate success
        RTS                     ; Return to caller
BUFWR8: SEC                     ; Indicate failure
BUFWR9: RTS                     ; Return to caller

U_PUTS: PHY
        LDY     $00
        PHY
        LDY     $01
        PHY
        PHA

        STX     $00
        STA     $01
        LDY     #$00
U_NEXT: LDA     ($00),Y
        BEQ     RET
        TAX
        LDA     #$20
U_WAIT: BIT     U_LSR
        BEQ     U_WAIT
        STX     U_THR
        INY
        BRA     U_NEXT

RET:    PLA
        PLY
        STY     $01
        PLY
        STY     $00
        PLY
        RTS

IRQ:
        PHA
        ; check the counter
        BIT     C_CTRL
        BVC     I_UART
        JSR     C_INIT
        LDA     C_CTRL          ; A = Counter control
        ORA     #%00000101      ; set Counter Enable and Counter Interrupt bits
        STA     C_CTRL          ; Store counter control
        BRA     I_END
I_UART:
        STP
        ; write string to buffer..
        ; check the UART write interrupt
        ; write character from buffer....
I_END:  PLA
NMI:    RTI

        .SEGMENT "RODATA"
TICK:   .BYTE    "All work and no play makes Jack a dull boy.", $0D, $0A, 0

        .SEGMENT "BSS"
BUFFER: .RES $100

        .SEGMENT "VECTOR"
V_NMI:  .WORD   NMI
V_RESET:.WORD   START
V_IRQ:  .WORD   IRQ
