;
; Copyright (c) 2013 kaytat
;
; Permission is hereby granted, free of charge, to any person obtaining a copy
; of this software and associated documentation files (the "Software"), to deal
; in the Software without restriction, including without limitation the rights
; to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
; copies of the Software, and to permit persons to whom the Software is
; furnished to do so, subject to the following conditions:
;
; The above copyright notice and this permission notice shall be included in
; all copies or substantial portions of the Software.
;
; THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
; IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
; FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
; AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
; LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
; OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
; THE SOFTWARE.
;

$include(tini_400.inc)
$include(ds80c400.inc)
$include(tinimacro.inc)
$include(apiequ.inc)

;
; Technically slush starts at 0x470100.  The first 256 bytes are used
; by the ipconfig command to store network parameters.  However, it's
; just easier to deal with addresses that start at a nice 64k boundary.
;
SLUSH_START_ADDRESS EQU 470000h

;
; The AMD part does not seem to care what the "tickle" address is,
; but leave this in there anyway.
;
FLASH_TICKLE0 EQU (SLUSH_START_ADDRESS or 5555h)

;
; Required initialization
;
reg_init:
    clr a
    ret

Native_erase_sector:
    push DPX3
    push DPH3
    push DPL3
    push DPX2
    push DPH2
    push DPL2

    ; Disable interrupts
    clr ea

    ; Parameter 0 - flash addr
    clr   A
    lcall NatLib_LoadPrimitive
    mov   DPX, R2
    mov   DPH, R1
    mov   DPL, R0

    ; Initialize tickle address pointer
    mov   DPS, #008h
    mov   DPTR, #FLASH_TICKLE0

    ; 1st Cycle
    mov    A, #0aah
    movx   @DPTR, A
    nop
    nop
    nop
    nop

    ; 2nd Cycle
    mov    A, #055h
    movx   @DPTR, A
    nop
    nop
    nop
    nop

    ; 3rd Cycle
    mov    A, #080h
    movx   @DPTR, A
    nop
    nop
    nop
    nop

    ; 4th Cycle
    mov    A, #0aah
    movx   @DPTR, A
    nop
    nop
    nop
    nop

    ; 5th Cycle
    mov    A, #055h
    movx   @DPTR, A
    nop
    nop
    nop
    nop

    ; 6th sector address and command 0x30
    ; Select the dptr0 and use that as the address
    mov    DPS, #000h
    mov    A, #030h
    movx   @DPTR, A

    ; Some delay before polling for status.  Necessary?
    nop
    nop
    nop
    nop

    ; Wait for operation to complete by checking to make sure the
    ; byte is 0xff, the reset value.
_erase_sector__wait:
    ; Some delay before polling for status.  Necessary?
    nop
    nop
    nop
    nop

    movx   A, @DPTR
    cjne   A, #0ffh, _erase_sector__wait

    pop   DPL2
    pop   DPH2
    pop   DPX2
    pop   DPL3
    pop   DPH3
    pop   DPX3

    ; Renable interrupts
    setb  EA

    clr    A
    ret

Native_program_block:
    push DPX3
    push DPH3
    push DPL3
    push DPX2
    push DPH2
    push DPL2

    ; Disable interrupts
    clr EA

    ; Parameter 0 - flash addr.  Store temporarily in B1
    clr   A
    lcall NatLib_LoadPrimitive
    mov   R2_B1, R2
    mov   R1_B1, R1
    mov   R0_B1, R0

    ; Parameter 1 - byte array
    mov   A, #001h
    lcall NatLib_LoadJavaByteArray
    mov   DPX1, DPX
    mov   DPH1, DPH
    mov   DPL1, DPL

    ; Also store the byte array size into the proper bank.  The NatLib_LoadJavaByteArray
    ; returns the byte array size in R{3:0}
    mov   R0_B3, R0
    mov   R1_B3, R1
    mov   R2_B3, R2
    mov   R3_B3, R3

    ; Setup flash addr
    mov   DPX, R2_B1
    mov   DPH, R1_B1
    mov   DPL, R0_B1

    ; Setup byte counter
    mov   DPL3, #000h
    mov   DPH3, #000h
    mov   DPX3, #000h

    ; Initialize tickle address pointer
    mov   DPS, #008h
    mov   DPTR, #FLASH_TICKLE0

    ; R1 - byte to write

_program_block__write_image_byte:
    ; Get the byte from SRAM by selecting the SRAM DPTR (DPTR1)
    mov    DPS, #001h
    movx   A, @DPTR
    inc    DPTR
    mov    R1, A

    ; Check against the current byte in flash
    mov    DPS, #0h
    movx   A, @DPTR
    xrl    A, R1
    jz     _program_block__check_write_image_done

    ; Write it
    mov    DPS, #008h

    ; 1st Cycle
    mov    A, #0aah
    movx   @DPTR, A

    ; 2nd Cycle
    mov    A, #55h
    movx   @DPTR, A

    ; 3rd Cycle
    mov    A, #0a0h
    movx   @DPTR, A

    ; 4th Cycle - put destination byte
    mov    DPS, #000h
    mov    A, R1
    movx   @DPTR, A

_program_block__write_image_byte_wait:
    movx   A, @DPTR
    cjne   A, R1_B0, _program_block__write_image_byte_wait

_program_block__check_write_image_done:
    ; Increment flash ptr
    inc    DPTR

    ; Increment byte counter
    mov   DPS, #009h
    inc   DPTR

    ; Check DPTR3 to see if all the bytes have been written
    mov   A, R0_B3
    cjne  A, DPL3, _program_block__write_image_byte

    mov   A, R1_B3
    cjne  A, DPH3, _program_block__write_image_byte

    mov   A, R2_B3
    cjne  A, DPX3, _program_block__write_image_byte

    pop   DPL2
    pop   DPH2
    pop   DPX2
    pop   DPL3
    pop   DPH3
    pop   DPX3

    ; Renable interrupts
    setb  EA

    clr A
    ret


;
; This is copied directly from reg.a51.  The only modification
; is to assume a value of 1 for the parameter.
;
; Backlight - P3.7
; RS        - P5.6
; R/W       - P5.5
; E         - P5.4
; Data      - Port 6 and bit 4 is P3.4
;
Native_clr_display:
    ; Drive the bits low so that if they need to go high,
    ; a strong pullup will be used
    mov a, P3
    anl a, #0EFh
    mov P3, a
    mov P5, #08Fh
    mov P6, #010h

    ; P6 = LCD[7-5,3-0] data with bit 4 set
    ; P3 = P3 with LCD[4]
    ;
    ; A == 0x01, for force a reset on the device
    ;
    mov a, #001h
    orl a, #010h
    mov P6, a
    mov a, R0
    anl a, #010h
    orl P3, a

    ; P5 = RS lo, R/W lo, E lo, other bits high.
    ; There hould be enough time for the setup time to be satisfied
    ; with just executing the instruction
    mov P5, #08Fh

    ; P5 |= E bit
    ; E bits needs to stay high for 23 ns.  Wait 2 machine cycles to be safe
    mov P5, #09Fh
    nop
    nop

    ; P5 = RS lo, R/W hi, E lo, other bits high.
    ; The hold time should be satisifed without waiting
    mov P5, #08Fh

    ; Return - the next time this is called, there should have
    ; been plenty of time elapsed to satisfy the LCD cycle time
    clr a
    ret

END
