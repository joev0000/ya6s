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

START:  JSR     U_INIT
        JSR     C_INIT

        LDA     C_CTRL
        ORA     #$01
        STA     C_CTRL

LOOP:   BIT     C_CTRL          ; Check the counter control bits
        BVC     LOOP            ; If bit 6 (counter zero) not set, loop.

        LDA     #>TICK
        LDX     #<TICK
        JSR     U_PUTS
        BRA     START

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

NMI:
IRQ:    RTI

TICK:   .BYTE    "All work and no play makes Jack a dull boy.", $0D, $0A, 0

        .SEGMENT "VECTOR"
V_NMI:  .WORD   NMI
V_RESET:.WORD   START
V_IRQ:  .WORD   IRQ
