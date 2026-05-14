.globl _main

_main:
    mov w0, #6
    sub sp, sp, #16
    str w0, [sp]
    mov w0, #3
    ldr w1, [sp], #16
    sdiv w0, w1, w0
    sub sp, sp, #16
    str w0, [sp]
    mov w0, #2
    ldr w1, [sp], #16
    sdiv w0, w1, w0
    ret
