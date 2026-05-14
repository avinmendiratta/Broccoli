import java.io.File

class Generator {

/*---------------------------Utilities-------------------------------------*/

    private fun StringBuilder.emit(line: String) {
        appendLine("    $line")
    }

    private fun mangle(name: String): String = "_$name"

    private var labelCounter = 0

    private fun freshLabel(prefix: String): String {
        return "_${prefix}_${labelCounter++}"
    }

    private fun alignedSize(env: Env): Int = ((env.nextOffset + 15) / 16) * 16

    private fun measureLocals(node: BlockItem, env: Env) {
        when (node) {
            is VarDeclaration -> {
                if (node.name !in env.locals) {
                    env.nextOffset += 4
                    env.locals[node.name] = env.nextOffset
                }
            }

            is Compound -> {
                val innerEnv = Env(
                    locals = env.locals.toMutableMap(),
                    nextOffset = env.nextOffset
                )

                node.body.forEach { item ->
                    if (item is VarDeclaration) {
                        if (item.name !in innerEnv.locals) {
                            measureLocals(item, innerEnv)

                        } else if (item.name in env.locals
                            && innerEnv.locals[item.name] == env.locals[item.name] ) {

                            innerEnv.nextOffset += 4
                            innerEnv.locals[item.name] = innerEnv.nextOffset
                        }

                    } else measureLocals(item, innerEnv)
                }

                env.nextOffset = innerEnv.nextOffset
            }

            is IfStatement -> {
                val thenEnv = Env(
                    locals = env.locals.toMutableMap(),
                    nextOffset = env.nextOffset
                )
                measureLocals(node.thenBranch, thenEnv)

                val elseEnv = Env(
                    locals = env.locals.toMutableMap(),
                    nextOffset = env.nextOffset
                )
                node.elseBranch?.let { measureLocals(it, elseEnv) }

                env.nextOffset = maxOf(thenEnv.nextOffset, elseEnv.nextOffset)
            }

            is WhileStatement -> measureLocals(node.body, env)

            is DoWhileStatement -> measureLocals(node.body, env)

            is ForStatement -> {
                measureLocals(node.body, env)
            }

            is ForDeclStatement -> {
                val innerEnv = Env(
                    locals = env.locals.toMutableMap(),
                    nextOffset = env.nextOffset
                )

                innerEnv.nextOffset += 4
                innerEnv.locals[node.initializer.name] = innerEnv.nextOffset
                measureLocals(node.body, innerEnv)

                env.nextOffset = innerEnv.nextOffset
            }

            is Return -> return
            is ExpStatement -> return
            is Break -> return
            is Continue -> return
        }
    }














/*------------------------------Data Structures----------------------------*/

    data class Env(
        val locals: MutableMap<String, Int> = mutableMapOf(),
        var nextOffset: Int = 0,

        val loopStack: ArrayDeque<LoopLabels> = ArrayDeque()
    )

    private var globalNames: MutableSet<String> = mutableSetOf()

    // locals is a map of local variable names to their offsets in the stack frame
    // nextOffset tracks “How much stack space have I already assigned?”
    // we basically make space in the stack by decrementing sp
    // and then use sp as a base to store variables starting from sp upwards


    // a declaration ought to update the original env
    // but, a statement copies the original env and updates its locals in that new env


    data class LoopLabels(
        val start: String,
        val continueLabel: String,
        val end: String
    )

    val stackUsage = mutableMapOf<String, Int>()

    var syscallCount = 0












/*---------------------------Code Generation-------------------------------*/

    fun codegen(program: Program, outputPath: String) {
        globalNames.clear()

        val asm = buildString {
            genProgram(program)
        }

        File(outputPath).writeText(asm)
    }

    private fun StringBuilder.genProgram(program: Program) {
        val globals = mutableListOf<GloblVar>()

        program.items.forEach { item ->
            if (item is GloblVar) globals.add(item)
        }

//        val siskaal =
//            program.items
//                .filterIsInstance<FunDecl>()
//                .filter { it.function.body != null }
//                .flatMap { it.function.body!! }
//                .filterIsInstance<ExpStatement>()
//                .any { it.expr is Syscall}

        if (syscallCount == 1 || globals.isNotEmpty()) appendLine(".data")

        if (syscallCount == 1) {
            appendLine(".align 3")
            appendLine("msg:")
            emit(".ascii \"Don ko pakadna mushkil hi nahi namumkin hai!\\n\"")
            appendLine("msglen = . - msg")
        }

        if (globals.isNotEmpty()) {
            appendLine(".align 2")

            globals.forEach { global ->
                globalNames.add(global.variable.name)

                appendLine(".globl " + mangle(global.variable.name))
                appendLine(mangle(global.variable.name) + ":")

                when (val init = global.variable.initializer) {
                    null -> emit(".word 0")
                    is IntegerLiteral -> emit(".word ${init.value}")
                    else -> error("Global initializer for '${global.variable.name}' must be a constant integer")
                }
            }
        }

        appendLine(".text")
        program.items.forEach { item ->
            if (item is FunDecl) genFunction(item.function)
        }
    }

    private fun StringBuilder.genFunction(function: Function) {

        stackUsage[function.name] = 0

        if (function.body == null) return

        // assign stack slots as positive offsets from fp
        val sizingEnv = Env()
        function.parameters?.forEach {
            sizingEnv.nextOffset += 4
            sizingEnv.locals[it] = sizingEnv.nextOffset
        }
        function.body.forEach { measureLocals(it, sizingEnv) }

        val stackSize = alignedSize(sizingEnv)
        stackUsage[function.name] = stackSize

        val env = Env() // fresh for codegen

        // --- PROLOGUE ---
        appendLine(".globl _${function.name}")
        appendLine("_${function.name}:")

        // old fp and return add. are now in sp and sp-8
        emit("stp x29, x30, [sp, #-16]!")
        emit("mov x29, sp")
        emit("sub sp, sp, #$stackSize")


        // assign parameters to stack slots
        function.parameters?.forEachIndexed { index, param ->
            env.nextOffset += 4
            env.locals[param] = env.nextOffset

            // move from argument registers into stack
            if (index < 8) {
                emit("str w$index, [x29, #-${env.nextOffset}]")
            } else {
                error("more than 8 params not supported yet")
            }
        }


        // --- BODY ---
        function.body.forEach { item ->
            when (item) {
                is VarDeclaration -> {
                    // Ensure every local declaration gets an offset before use.
                    if (item.name !in env.locals) {
                        env.nextOffset += 4
                        env.locals[item.name] = env.nextOffset
                    }

                    if (item.initializer != null) {
                        genExpression(item.initializer, env)
                        val offset = env.locals[item.name]
                            ?: error("Failed to allocate local variable ${item.name}")
                        emit("str w0, [x29, #-$offset]")
                    }
                }

                is Statement -> genStatement(item, env)
            }
        }

        // --- DEFAULT RETURN ---
        // --- EPILOGUE ---

        emit("mov w0, #0")
        emit("mov sp, x29")              // de-allocate stack space
        emit("ldp x29, x30, [sp], #16")  // restore fp and return address
        emit("ret")                      // return to caller
    }

    fun StringBuilder.genStatement(stmt: Statement, env: Env) {
        when (stmt) {

            is Return -> {
                genExpression(stmt.expr, env)
                emit("mov sp, x29")
                emit("ldp x29, x30, [sp], #16")
                emit("ret")
            }

            is ExpStatement -> {
                genExpression(stmt.expr, env)
            }

            is IfStatement -> {
                val elseLabel = freshLabel("else")
                val endLabel = freshLabel("endif")

                genExpression(stmt.condition, env)
                emit("cmp w0, #0")

                if (stmt.elseBranch != null) {
                    emit("beq $elseLabel")

                    genStatement(stmt.thenBranch, env)
                    emit("b $endLabel")

                    emit("$elseLabel:")
                    genStatement(stmt.elseBranch, env)

                    emit("$endLabel:")

                } else {
                    emit("beq $endLabel")
                    genStatement(stmt.thenBranch, env)
                    emit("$endLabel:")
                }
            }

            is WhileStatement -> {
                val start = freshLabel("while_start")
                val end = freshLabel("while_end")

                env.loopStack.addLast(LoopLabels(start, start, end))

                appendLine("$start:")

                genExpression(stmt.condition, env)
                emit("cmp w0, #0")
                emit("beq $end")

                genStatement(stmt.body, env)

                emit("b $start")

                appendLine("$end:")

                env.loopStack.removeLast()
            }

            is DoWhileStatement -> {
                val start = freshLabel("do_start")
                val cond = freshLabel("do_cond")
                val end = freshLabel("do_end")

                env.loopStack.addLast(LoopLabels(start, cond, end))

                appendLine("$start:")

                genStatement(stmt.body, env)

                appendLine("$cond:")

                genExpression(stmt.condition, env)
                emit("cmp w0, #0")
                emit("bne $start")

                appendLine("$end:")

                env.loopStack.removeLast()
            }

            is ForStatement -> genForStatement(stmt, env)

            is ForDeclStatement -> genForDeclStatement(stmt, env)

            is Break -> {
                val loop = env.loopStack.lastOrNull()
                    ?: error("break not inside loop")

                emit("b ${loop.end}")
            }

            is Continue -> {
                val loop = env.loopStack.lastOrNull()
                    ?: error("continue not inside loop")

                emit("b ${loop.continueLabel}")
            }

            is Compound -> {
                val innerEnv = Env(
                    locals = env.locals.toMutableMap(),
                    nextOffset = env.nextOffset,
                    loopStack = env.loopStack
                )

                stmt.body.forEach { item ->
                    when (item) {
                        is VarDeclaration -> {
                            if (item.name !in innerEnv.locals) {
                                innerEnv.nextOffset += 4
                                innerEnv.locals[item.name] = innerEnv.nextOffset

                            } else if (item.name in env.locals
                                && innerEnv.locals[item.name] == env.locals[item.name] ) {

                                innerEnv.nextOffset += 4
                                innerEnv.locals[item.name] = innerEnv.nextOffset

                            } else error("Duplicate local variable ${item.name}")

                            if (item.initializer != null) {
                                genExpression(item.initializer, innerEnv)
                                val offset = innerEnv.locals[item.name]!!
                                emit("str w0, [x29, #-$offset]")
                            }
                        }

                        is Statement -> genStatement(item, innerEnv)
                    }
                }

                // propagate offset forward
                env.nextOffset = innerEnv.nextOffset
            }
        }
    }

    private fun StringBuilder.genForStatement(stmt: ForStatement, env: Env) {
        val start = freshLabel("for_start")
        val continueLabel = freshLabel("for_continue")
        val end = freshLabel("for_end")

        // init
        genExpression(stmt.initializer, env)

        env.loopStack.addLast(LoopLabels(start, continueLabel, end))

        appendLine("$start:")

        // condition
        genExpression(stmt.condition, env)
        emit("cmp w0, #0")
        emit("beq $end")

        // body
        genStatement(stmt.body, env)

        appendLine("$continueLabel:")

        // cycle
        genExpression(stmt.cycle, env)

        emit("b $start")

        appendLine("$end:")

        env.loopStack.removeLast()
    }

    private fun StringBuilder.genForDeclStatement(stmt: ForDeclStatement, env: Env) {

        val innerEnv = Env(
            locals = env.locals.toMutableMap(),
            nextOffset = env.nextOffset,
            loopStack = env.loopStack
        )

        val start = freshLabel("for_start")
        val continueLabel = freshLabel("for_continue")
        val end = freshLabel("for_end")

        // allocate variable (same as VarDeclaration logic)
        innerEnv.nextOffset += 4
        innerEnv.locals[stmt.initializer.name] = innerEnv.nextOffset

        stmt.initializer.initializer?.let {
            genExpression(it, innerEnv)
            val offset = innerEnv.locals[stmt.initializer.name]!!
            emit("str w0, [x29, #-$offset]")
        }

        innerEnv.loopStack.addLast(LoopLabels(start, continueLabel, end))

        appendLine("$start:")

        genExpression(stmt.condition, innerEnv)
        emit("cmp w0, #0")
        emit("beq $end")

        genStatement(stmt.body, innerEnv)

        appendLine("$continueLabel:")

        genExpression(stmt.cycle, innerEnv)

        emit("b $start")

        appendLine("$end:")

        // propagate offset forward
        env.nextOffset = innerEnv.nextOffset

        innerEnv.loopStack.removeLast()
    }



    // abhi ke liye toh we are always storing the evaluated expression in w0
    // but when moving on to multi-register codegen
    // should abstract registers like w0 as
    // val reg = "w0"
    // emit("neg $reg, $reg")
    // or I think cud make array to register to abstract all 32

    private fun StringBuilder.genExpression(expr: Expression?, env: Env) {
        when (expr) {
            null -> return

            is IntegerLiteral ->
                emit("mov w0, #${expr.value}")

            // ain't got no offset? yo var don't exist
            is Variable -> {
                if (expr.name in env.locals) {
                    val offset = env.locals[expr.name]
                    emit("ldr w0, [x29, #-$offset]")

                } else if (expr.name in globalNames) {
                    emit("adrp x1, ${mangle(expr.name + "@PAGE")}")
                    emit("add x1, x1, ${mangle(expr.name + "@PAGEOFF")}")
                    emit("ldr w0, [x1]")

                } else error("Undefined variable ${expr.name}")
            }

            is Assignment -> {
                // exp value is now in w0
                genExpression(expr.expr, env)

                if (expr.name in env.locals) {
                    val offset = env.locals[expr.name]
                    emit("str w0, [x29, #-$offset]")

                } else if (expr.name in globalNames) {
                    emit("adrp x1, ${mangle(expr.name + "@PAGE")}")
                    emit("add x1, x1, ${mangle(expr.name + "@PAGEOFF")}")
                    emit("str w0, [x1]")

                } else error("Undefined variable ${expr.name}")
            }

            is FunctionCall -> {
                // evaluate all args left → right and push
                emit("sub sp, sp, #32")
                expr.arguments.forEachIndexed { index, arg ->
                    genExpression(arg, env)
                    emit("str w0, [sp, #${index * 4}]")
                }

                // pop into registers in reverse
                expr.arguments.indices.reversed().forEach { index ->
                    if (index < 8) {
                        emit("ldr w$index, [sp, #${index * 4}]")
                    } else {
                        error("more than 8 args not supported yet")
                    }
                }
                emit("add sp, sp, #32")

                emit("bl ${mangle(expr.name)}")
                // result already in w0
            }

            is Syscall -> {
                emit("mov x0, #1")
                emit("adrp x1, msg@PAGE")
                emit("add x1, x1, msg@PAGEOFF")
                emit("mov x2, msglen")

                // 4 is syscall ID for write
                emit("mov x16, #4")
                // call kernel
                emit("svc #0x80")
            }

            is Unary -> {
                genExpression(expr.expr, env)
                when (expr.operator) {
                    UnanOp.MINUS -> emit("neg w0, w0")
                    UnanOp.BITCOMP -> emit("mvn w0, w0")
                    UnanOp.LOGICNOT -> {
                        emit("cmp w0, #0")
                        emit("cset w0, eq")
                    }
                }
            }

            is Conditional -> {
                val endifLabel = freshLabel("endif")
                val elseLabel = freshLabel("else")

                // condition is now in w0
                genExpression(expr.condition, env)
                emit("cmp w0, #0")
                emit("beq $elseLabel")

                genExpression(expr.thenBranch, env)
                emit("b $endifLabel")

                emit("$elseLabel:")
                genExpression(expr.elseBranch, env)

                emit("$endifLabel:")
            }

            is Binary -> {

                if (expr.operator == BiOp.AND) {
                    val labelFalse = freshLabel("and_false")
                    val labelEnd = freshLabel("and_end")

                    genExpression(expr.left, env)
                    emit("cmp w0, #0")
                    emit("beq $labelFalse")

                    genExpression(expr.right, env)
                    emit("cmp w0, #0")
                    emit("cset w0, ne")
                    emit("b $labelEnd")

                    emit("$labelFalse:")
                    emit("mov w0, #0")

                    emit("$labelEnd:")

                    return
                }

                if (expr.operator == BiOp.OR) {
                    val labelTrue = freshLabel("or_true")
                    val labelEnd = freshLabel("or_end")

                    genExpression(expr.left, env)
                    emit("cmp w0, #0")
                    emit("bne $labelTrue")

                    genExpression(expr.right, env)
                    emit("cmp w0, #0")
                    emit("cset w0, ne")
                    emit("b $labelEnd")

                    emit("$labelTrue:")
                    emit("mov w0, #1")

                    emit("$labelEnd:")

                    return
                }

                // left exp is now in w0
                genExpression(expr.left, env)

                // we are always making a stack frame of 16 bytes
                // to maintain stack alignment and be ABI compliant

                // put w0 in stack
                emit("str w0, [sp, #-16]!")   // pushed
             // emit("sub sp, sp, #16")
             // emit("str w0, [sp]")
                // the above emit decrements
                // and then stores the value using the same instruction
                // this is because of the "!" at the end of the instruction

                // three addressing modes
                //  1. pre-indexed: str w0, [sp, #-16]!
                //  2. post-indexed: str w0, [sp], #16
                //  3. offset: str w0, [sp, #-16]
                // the first two modes are used for stack operations
                // as they automatically update the stack pointer
                // the third mode is used for accessing local variables
                // or function parameters stored on the stack

                // right exp is now in w0
                genExpression(expr.right, env)

                // left exp is now in w1
                emit("ldr w1, [sp], #16")   // popped

                // do: w0 = w1 op w0
                when (expr.operator) {
                    // relational
                    BiOp.EQ -> {
                        emit("cmp w1, w0")
                        emit("cset w0, eq")
                    }
                    BiOp.NEQ -> {
                        emit("cmp w1, w0")
                        emit("cset w0, ne")
                    }
                    BiOp.LT -> {
                        emit("cmp w1, w0")
                        emit("cset w0, lt")
                    }
                    BiOp.LTE -> {
                        emit("cmp w1, w0")
                        emit("cset w0, le")
                    }
                    BiOp.GT -> {
                        emit("cmp w1, w0")
                        emit("cset w0, gt")
                    }
                    BiOp.GTE -> {
                        emit("cmp w1, w0")
                        emit("cset w0, ge")
                    }

                    // arithmetic
                    BiOp.PLUS -> emit("add w0, w1, w0")
                    BiOp.MINUS -> emit("sub w0, w1, w0")
                    BiOp.MUL -> emit("mul w0, w1, w0")
                    BiOp.DIV -> emit("sdiv w0, w1, w0")
                    BiOp.MOD -> {
                        emit("sdiv w2, w1, w0")      // w2 = a / b              where a = w1 and b = w0

                        emit("mul w2, w2, w0")       // w2 = (a * b) / b        since the decimals get chopped off
                        emit("sub w0, w1, w2")       // w0 = a - (a * b)        i.e., remainder

                        // cleaner way to do this, after emitting sdiv:
                    //  msub w0, w2, w1, w0                // w0 = w0 - (w2 * w1)     multiply then subtract in one instruction
                    }

                    // bitwise
                    BiOp.BITAND -> emit("and w0, w1, w0")
                    BiOp.BITOR -> emit("orr w0, w1, w0")
                    BiOp.BITXOR -> emit("eor w0, w1, w0")
                    BiOp.LSHIFT -> emit("lsl w0, w1, w0")
                    BiOp.RSHIFT -> emit("asr w0, w1, w0")
                    // lsl is logical shift left which shifts bits without a care for the sign bit
                    // lsl will work same for signed and unsigned integers - why?
                    // because the sign bit is not shifted
                    // asr is arithmetic shift right wich takes care of the sign bit
                    // lsr doesn't work for signed integers

                    // logical

                    else -> error("Unknown binary operator ${expr.operator}")
                }
            }
        }
    }

}


// look into better assembly debugging methods like GDB and LLDB using instruction like stepi and nexti to step through instructions