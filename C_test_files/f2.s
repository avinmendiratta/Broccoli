.text
.globl _add
_add:
    stp x29, x30, [sp, #-16]!
    mov x29, sp
    sub sp, sp, #16
    str w0, [x29, #-4]
    str w1, [x29, #-8]
    ldr w0, [x29, #-4]
    str w0, [sp, #-16]!
    ldr w0, [x29, #-8]
    ldr w1, [sp], #16
    add w0, w1, w0
    mov sp, x29
    ldp x29, x30, [sp], #16
    ret
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
    sub sp, sp, #32
    mov w0, #1
    str w0, [sp, #-16]!
    mov w0, #2
    ldr w1, [sp], #16
    add w0, w1, w0
    str w0, [sp, #0]
    mov w0, #4
    str w0, [sp, #4]
    ldr w1, [sp, #4]
    ldr w0, [sp, #0]
    add sp, sp, #32
    bl _add
    str w0, [x29, #-4]
    ldr w0, [x29, #-4]
    str w0, [sp, #-16]!
    ldr w0, [x29, #-4]
    ldr w1, [sp], #16
    add w0, w1, w0
    mov sp, x29
    ldp x29, x30, [sp], #16
    ret
    mov w0, #0
    mov sp, x29
    ldp x29, x30, [sp], #16
    ret
