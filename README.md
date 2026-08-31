# Broccoli
## Brave OS-aware CC On Line

A minimal C subset compiler written in Kotlin that targets ARM64 (AArch64) assembly for Apple Silicon macOS systems.

Broccoli is envisioned as a systems-oriented compiler project that serves as a learning bridge between compiler construction and operating systems concepts by generating real executable binaries that follow the macOS ARM64 ABI and runtime conventions.

---

## Overview

This toy compiler generates valid ARM64 assembly and follows Apple Silicon calling convention thereby emphasizing ABI correctness and highlighting fundamental low-level execution details.

The implemetation was adapted from Nora Sandler's "Writing a C Compiler" which served as a fundamentals' guide to understanding compiler architecture and construction.

---

## Language Features

- Primitive types:
  - `int`
  - `char*`

- Variables:
  - Local variables
  - Global variables

- Operators:
  - Unary operators
  - Arithmetic operators
  - Relational operators
  - Logical operators
  - Bitwise operators

- Control flow:
  - `if` / `else`
  - `while`
  - `for`

- Functions:
  - Function definitions
  - Parameters
  - Return statements
  - Recursion

---

## Non-Goals

Unsupported features include:

- Preprocessor macros
- Structs and unions
- Dynamic memory allocation
- Multi-file compilation
- Advanced optimization passes
- Full ISO C standard support

---

## Technical Architecture

```text
Source Code
    ↓
Lexer
    ↓
Recursive Descent Parser
    ↓
AST Construction
    ↓
Semantic Analysis
    ↓
ARM64 Code Generator
    ↓
Assembly Output (.s)
    ↓
clang / ld
    ↓
Mach-O Executable
```