; Copyright (C) 2021, 2022 Joseph Vigneau

        .EXPORT BUFINI
        .EXPORT BUFRD
        .EXPORT BUFWR
        .EXPORT BPUTS

        .SEGMENT "CODE"
        .PC02

;
; Initialize the ring buffer
;
; The first byte of the ring buffer is the read index.
; The second bytes of the ring buffer is the write index.
;
BUFINI: PHA                     ; Save A
        LDA     #$02            ; X = 2
        STA     BUFFER+0        ; Set Read Index to 2
        STA     BUFFER+1        ; Set Write Index to 2
        PLA                     ; Restore A
        RTS                     ; Return to caller

;
; Load the next byte from the ring buffer into A.
; Set carry flag if no byte is available.
;
; Algorithm:
;   if [0] == [1], the buffer is empty.
;   else [0]++, return [0].
;

BUFRD:  PHX                     ; Save X

        LDX     BUFFER+0        ; X = Read Index
        CPX     BUFFER+1        ; Compare with the Write Index
        BEQ     @EMPTY          ; If same, signal buffer empty
        LDA     BUFFER,X        ; Load the byte from the buffer
        INX                     ; Increment the read pointer
        CPX     #$00            ; Did it wrap around?
        BNE     @NOWRAP         ; If not, skip ahead
        LDX     #$02            ; Set the read index to the beginning
@NOWRAP:STX     BUFFER+0        ; Store the read index
        CLC                     ; Indicate success
        BRA     @RET            ; Skip ahead to return
@EMPTY: SEC                     ; Indicate failure
@RET:   PLX                     ; Restore X
        RTS                     ; Return to caller

;
; Store the byte in A into the ring buffer.
; Set carry flag if there is no space in the buffer.

; Algorithm:
;   a = [1]+1
;   if a = [0] the buffer is full
;   else write to [1], [1] = a
;
BUFWR:  PHX                     ; Stash X
        PHY                     ; Stash Y
        LDX     BUFFER+1        ; X = Write Index
        PHX                     ; Push Write Index
        INX                     ; Increment Write Index
        BNE     @NOWRAP         ; If not zero, skip ahead
        LDX     #$02            ; Wrap around
@NOWRAP:PHX                     ; Transfer incremented Write Index
        PLY                     ;   from X to Y
        PLX                     ; Restore original Write Index
        CPY     BUFFER+0        ; Compare incremented index with read index
        BEQ     @FULL           ; If equal, the buffer is full, skip ahead.
        STA     BUFFER,X        ; Store the byte at the write index
        STY     BUFFER+1        ; Save the incremented write index
        CLC                     ; Indicate success
        PLY                     ; Restore Y
        PLX                     ; Restore X
        RTS                     ; Return to caller
@FULL:  SEC                     ; Indicate failure
        PLY                     ; Restore Y
        PLX                     ; Restore X
        RTS                     ; Return to caller

; Write the string in AX into the ring buffer.
; Overwrites A.
BPUTS:
        PHY                     ; Stash Y
        LDY     $00             ; Load the value at $00
        PHY                     ; Stash $00
        LDY     $01             ; Load the value at $01
        PHY                     ; Stash $01

        STX     $00             ; Store lo address
        STA     $01             ; Store hi address
        LDY     #$00            ; Reset index

@NEXT:  LDA     ($00),Y         ; Load byte
        BEQ     @RET            ; If zero, we're done
        INY                     ; Increment index
        JSR     BUFWR           ; Store it into the ring buffer
        BCC     @NEXT           ; If there's still space in the buffer,
                                ;   go to next byte

@RET:   PLY                     ; Recover $01
        STY     $01             ; Store it
        PLY                     ; Recover $00
        STY     $00             ; Store it
        PLY                     ; Restore Y
        RTS                     ; Return to caller

        .SEGMENT "BSS"
BUFFER: .RES $100

