.text
.globl _sub_3
_sub_3:
    stp x29, x30, [sp, #-16]!
    mov x29, sp
    sub sp, sp, #16
    str w0, [x29, #-4]
    str w1, [x29, #-8]
    str w2, [x29, #-12]
    ldr w0, [x29, #-4]
    str w0, [sp, #-16]!
    ldr w0, [x29, #-8]
    ldr w1, [sp], #16
    sub w0, w1, w0
    str w0, [sp, #-16]!
    ldr w0, [x29, #-12]
    ldr w1, [sp], #16
    sub w0, w1, w0
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
    sub sp, sp, #0
    sub sp, sp, #32
    mov w0, #10
    str w0, [sp, #0]
    mov w0, #4
    str w0, [sp, #4]
    mov w0, #2
    str w0, [sp, #8]
    ldr w2, [sp, #8]
    ldr w1, [sp, #4]
    ldr w0, [sp, #0]
    add sp, sp, #32
    bl _sub_3
    mov sp, x29
    ldp x29, x30, [sp], #16
    ret
    mov w0, #0
    mov sp, x29
    ldp x29, x30, [sp], #16
    ret
