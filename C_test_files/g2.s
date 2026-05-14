.text
.globl _foo
_foo:
    stp x29, x30, [sp, #-16]!
    mov x29, sp
    sub sp, sp, #0
    mov w0, #3
    mov sp, x29
    ldp x29, x30, [sp], #16
    ret
    mov w0, #0
    mov sp, x29
    ldp x29, x30, [sp], #16
    ret
.globl _main
_main:
    stp x29, x30, [sp, #-16]!
    mov x29, sp
    sub sp, sp, #16
    mov w0, #5
    str w0, [x29, #-4]
    ldr w0, [x29, #-4]
    mov sp, x29
    ldp x29, x30, [sp], #16
    ret
    mov w0, #0
    mov sp, x29
    ldp x29, x30, [sp], #16
    ret
