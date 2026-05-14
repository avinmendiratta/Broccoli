.text
.globl _main
_main:
    stp x29, x30, [sp, #-16]!
    mov x29, sp
    sub sp, sp, #0
    sub sp, sp, #32
    mov w0, #72
    str w0, [sp, #0]
    ldr w0, [sp, #0]
    add sp, sp, #32
    bl _putchar
    sub sp, sp, #32
    mov w0, #101
    str w0, [sp, #0]
    ldr w0, [sp, #0]
    add sp, sp, #32
    bl _putchar
    sub sp, sp, #32
    mov w0, #108
    str w0, [sp, #0]
    ldr w0, [sp, #0]
    add sp, sp, #32
    bl _putchar
    sub sp, sp, #32
    mov w0, #108
    str w0, [sp, #0]
    ldr w0, [sp, #0]
    add sp, sp, #32
    bl _putchar
    sub sp, sp, #32
    mov w0, #111
    str w0, [sp, #0]
    ldr w0, [sp, #0]
    add sp, sp, #32
    bl _putchar
    sub sp, sp, #32
    mov w0, #44
    str w0, [sp, #0]
    ldr w0, [sp, #0]
    add sp, sp, #32
    bl _putchar
    sub sp, sp, #32
    mov w0, #32
    str w0, [sp, #0]
    ldr w0, [sp, #0]
    add sp, sp, #32
    bl _putchar
    sub sp, sp, #32
    mov w0, #87
    str w0, [sp, #0]
    ldr w0, [sp, #0]
    add sp, sp, #32
    bl _putchar
    sub sp, sp, #32
    mov w0, #111
    str w0, [sp, #0]
    ldr w0, [sp, #0]
    add sp, sp, #32
    bl _putchar
    sub sp, sp, #32
    mov w0, #114
    str w0, [sp, #0]
    ldr w0, [sp, #0]
    add sp, sp, #32
    bl _putchar
    sub sp, sp, #32
    mov w0, #108
    str w0, [sp, #0]
    ldr w0, [sp, #0]
    add sp, sp, #32
    bl _putchar
    sub sp, sp, #32
    mov w0, #100
    str w0, [sp, #0]
    ldr w0, [sp, #0]
    add sp, sp, #32
    bl _putchar
    sub sp, sp, #32
    mov w0, #33
    str w0, [sp, #0]
    ldr w0, [sp, #0]
    add sp, sp, #32
    bl _putchar
    sub sp, sp, #32
    mov w0, #10
    str w0, [sp, #0]
    ldr w0, [sp, #0]
    add sp, sp, #32
    bl _putchar
    mov w0, #0
    mov sp, x29
    ldp x29, x30, [sp], #16
    ret
