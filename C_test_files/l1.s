.text
.globl _main
_main:
    stp x29, x30, [sp, #-16]!
    mov x29, sp
    sub sp, sp, #16
    mov w0, #1
    str w0, [x29, #-4]
_do_start_0:
    sub sp, sp, #0
    ldr w0, [x29, #-4]
    mov w1, w0
    mov w0, #2
    mul w0, w1, w0
    str w0, [x29, #-4]
    add sp, sp, #0
_do_cond_1:
    ldr w0, [x29, #-4]
    mov w1, w0
    mov w0, #11
    cmp w1, w0
    cset w0, lt
    cmp w0, #0
    bne _do_start_0
_do_end_2:
    ldr w0, [x29, #-4]
    mov sp, x29
    ldp x29, x30, [sp], #16
    ret
    mov w0, #0
    mov sp, x29
    ldp x29, x30, [sp], #16
    ret
