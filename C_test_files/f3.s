.text
.globl _fib
_fib:
    stp x29, x30, [sp, #-16]!
    mov x29, sp
    sub sp, sp, #16
    str w0, [x29, #-4]
    ldr w0, [x29, #-4]
    str w0, [sp, #-16]!
    mov w0, #0
    ldr w1, [sp], #16
    cmp w1, w0
    cset w0, eq
    cmp w0, #0
    bne _or_true_2
    ldr w0, [x29, #-4]
    str w0, [sp, #-16]!
    mov w0, #1
    ldr w1, [sp], #16
    cmp w1, w0
    cset w0, eq
    cmp w0, #0
    cset w0, ne
    b _or_end_3
    _or_true_2:
    mov w0, #1
    _or_end_3:
    cmp w0, #0
    beq _else_0
    ldr w0, [x29, #-4]
    mov sp, x29
    ldp x29, x30, [sp], #16
    ret
    b _endif_1
    _else_0:
    sub sp, sp, #32
    ldr w0, [x29, #-4]
    str w0, [sp, #-16]!
    mov w0, #1
    ldr w1, [sp], #16
    sub w0, w1, w0
    str w0, [sp, #0]
    ldr w0, [sp, #0]
    add sp, sp, #32
    bl _fib
    str w0, [sp, #-16]!
    sub sp, sp, #32
    ldr w0, [x29, #-4]
    str w0, [sp, #-16]!
    mov w0, #2
    ldr w1, [sp], #16
    sub w0, w1, w0
    str w0, [sp, #0]
    ldr w0, [sp, #0]
    add sp, sp, #32
    bl _fib
    ldr w1, [sp], #16
    add w0, w1, w0
    mov sp, x29
    ldp x29, x30, [sp], #16
    ret
    _endif_1:
    mov w0, #0
    mov sp, x29
    ldp x29, x30, [sp], #16
    ret
.text
.globl _main
_main:
    stp x29, x30, [sp, #-16]!
    mov x29, sp
    sub sp, sp, #16
    mov w0, #5
    str w0, [x29, #-4]
    sub sp, sp, #32
    ldr w0, [x29, #-4]
    str w0, [sp, #0]
    ldr w0, [sp, #0]
    add sp, sp, #32
    bl _fib
    mov sp, x29
    ldp x29, x30, [sp], #16
    ret
    mov w0, #0
    mov sp, x29
    ldp x29, x30, [sp], #16
    ret
