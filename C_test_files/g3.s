.data
.globl _a
_a:
    .word 3
.text
.globl _main
_main:
    stp x29, x30, [sp, #-16]!
    mov x29, sp
    sub sp, sp, #16
    mov w0, #0
    str w0, [x29, #-4]
    adrp x1, _a@PAGE
    add x1, x1, _a@PAGEOFF
    ldr w0, [x1]
    cmp w0, #0
    beq _endif_1
    mov w0, #0
    str w0, [x29, #-8]
    mov w0, #4
    str w0, [x29, #-4]
    _endif_1:
    ldr w0, [x29, #-4]
    mov sp, x29
    ldp x29, x30, [sp], #16
    ret
    mov w0, #0
    mov sp, x29
    ldp x29, x30, [sp], #16
    ret
