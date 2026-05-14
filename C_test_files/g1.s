.data
.globl _foo
_foo:
    .word 0
.globl _foo
_foo:
    .word 3
.text
.globl _main
_main:
    stp x29, x30, [sp, #-16]!
    mov x29, sp
    sub sp, sp, #0
    adrp x1, _foo@PAGE
    add x1, x1, _foo@PAGEOFF
    ldr w0, [x1]
    mov sp, x29
    ldp x29, x30, [sp], #16
    ret
    mov w0, #0
    mov sp, x29
    ldp x29, x30, [sp], #16
    ret
