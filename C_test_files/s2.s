.text
.globl _main
_main:
    stp x29, x30, [sp, #-16]!
    mov x29, sp
    sub sp, sp, #16
    mov w0, #0
    str w0, [x29, #4]
    ldr w0, [x29, #4]
    cmp w0, #0
    beq _else_0
    sub sp, sp, #4
    mov w0, #2
    str w0, [sp]
    ldr w0, [x29, #8]
    mov sp, x29
    ldp x29, x30, [sp], #16
    ret
    add sp, sp, #4
    b _endif_1
    _else_0:
    sub sp, sp, #4
    mov w0, #3
    str w0, [sp]
    ldr w0, [x29, #4]
    sub sp, sp, #16
    str w0, [sp]
    ldr w0, [x29, #8]
    ldr w1, [sp], #16
    cmp w1, w0
    cset w0, lt
    cmp w0, #0
    beq _else_2
    mov w0, #4
    mov sp, x29
    ldp x29, x30, [sp], #16
    ret
    b _endif_3
    _else_2:
    mov w0, #5
    mov sp, x29
    ldp x29, x30, [sp], #16
    ret
    _endif_3:
    add sp, sp, #4
    _endif_1:
    ldr w0, [x29, #4]
    mov sp, x29
    ldp x29, x30, [sp], #16
    ret
    mov w0, #0
    mov sp, x29
    ldp x29, x30, [sp], #16
    ret
