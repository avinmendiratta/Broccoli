.data
.align 3
msg:
    .ascii "Don ko pakadna mushkil hi nahi namumkin hai!\n"
msglen = . - msg
.text
.globl _main
_main:
    stp x29, x30, [sp, #-16]!
    mov x29, sp
    sub sp, sp, #0
    mov x0, #1
    adrp x1, msg@PAGE
    add x1, x1, msg@PAGEOFF
    mov x2, msglen
    mov x16, #4
    svc #0x80
    mov w0, #0
    mov sp, x29
    ldp x29, x30, [sp], #16
    ret
