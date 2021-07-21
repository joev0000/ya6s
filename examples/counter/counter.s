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

START:  LDA     C_CTRL          ; A = Counter control
        AND     #$FA            ; Clear counter and interrupt enable bits
        STA     C_CTRL          ; Store counter control

        LDA     #$40            ; A = Low bits of 1000000(10)
        STA     C_LO            ; Store them in counter low
        LDA     #$42            ; A = Middle bits of 1000000(10)
        STA     C_MID           ; Store them in counter mid
        LDA     #$0F            ; A = High bits of 1000000(10)
        STA     C_HI            ; Store them in counter high

        LDA     C_CTRL          ; A = Counter control
        ORA     #$01            ; set counter enable bit
        STA     C_CTRL          ; Store counter control

LOOP:   BIT     C_CTRL          ; Check the counter control bits
        BVC     LOOP            ; If bit 6 (counter zero) not set, loop.

        STP                     ; Halt

NMI:
IRQ:    RTI

        .SEGMENT "VECTOR"
V_NMI:  .WORD   NMI
V_RESET:.WORD   START
V_IRQ:  .WORD   IRQ
