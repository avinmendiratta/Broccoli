# Broccoli
### Brave OS-aware CC On Line

A minimal yet non-trivial C subset compiler written in Kotlin that targets ARM64 (AArch64) assembly for Apple Silicon macOS systems.

Broccoli is designed as a systems-oriented compiler project that bridges the gap between compiler construction and operating systems concepts by generating real executable binaries that follow the macOS ARM64 ABI and runtime conventions.

---

## Overview

Most educational compiler projects stop at parsing or intermediate representation generation. Broccoli goes further by:

- Generating valid ARM64 assembly
- Following the Apple Silicon calling convention
- Producing Mach-O executables through the macOS toolchain
- Demonstrating stack frame construction and recursion
- Enabling runtime memory and process analysis

The project emphasizes low-level execution details, ABI correctness, and operating-system-aware code generation.

---

## Features

### Supported Language Features

- Primitive types:
  - `int`
  - `char`

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

Broccoli intentionally does **not** aim for full C compliance.

Unsupported features include:

- Preprocessor macros
- Structs and unions
- Dynamic memory allocation
- Multi-file compilation
- Advanced optimization passes
- Full ISO C standard support

---

## Technical Architecture

### Compiler Pipeline

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