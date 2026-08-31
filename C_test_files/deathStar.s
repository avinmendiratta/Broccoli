.text
.globl _main
_main:
    stp x29, x30, [sp, #-16]!
    mov x29, sp
    sub sp, sp, #0
    mov w0, #65
    str w0, [sp, #-16]!
    mov w0, #97
    ldr w1, [sp], #16
    cmp w1, w0
    cset w0, gt
    mov sp, x29
    ldp x29, x30, [sp], #16
    ret
    mov w0, #0
    mov sp, x29
    ldp x29, x30, [sp], #16
    ret
