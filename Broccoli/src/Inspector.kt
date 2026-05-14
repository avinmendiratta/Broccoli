// semantic analysis

class Inspector {

    var syscallCount = 0

    data class FunctionInfo(
        val name: String,
        val paramCount: Int,
        val hasBody: Boolean
    )

    fun validate(program: Program) {
        val (functionTable, globalVars) = collectTopLevel(program)
        validateFunctions(program, functionTable, globalVars)
    }

    // -------- Phase 1: collect and validate top-level symbols --------
    private fun collectTopLevel(program: Program): Pair<Map<String, FunctionInfo>, Map<String, Boolean>> {
        val functionTable = mutableMapOf<String, FunctionInfo>()
        val globalVars = mutableMapOf<String, Boolean>()
        // bool here show if initialized or not

        // External prototype(s) allowed without a body
        // functionTable["putchar"] = FunctionInfo("putchar", 1, false)

        for (item in program.items) {
            when (item) {
                is FunDecl -> {
                    val funDecl = item.function
                    val paramCount = funDecl.parameters?.size ?: 0
                    val hasBody = (funDecl.body != null)

                    // Rule 4: symbol not also declared as a variable
                    if (funDecl.name in globalVars) {
                        error("'${funDecl.name}' declared as both a function and a variable")
                    }

                    val existing = functionTable[funDecl.name]
                    if (existing == null) {
                        functionTable[funDecl.name] = FunctionInfo(funDecl.name, paramCount, hasBody)
                        continue
                    }

                    if (existing.paramCount != paramCount) {
                        error(
                            "Conflicting declarations for function ${funDecl.name}: " +
                                    "expected ${existing.paramCount} params but got $paramCount"
                        )
                    }

                    if (existing.hasBody && hasBody) {
                        error("Duplicate function definition: ${funDecl.name}")
                    }

                    functionTable[funDecl.name] = FunctionInfo(
                        funDecl.name,
                        paramCount,
                        existing.hasBody || hasBody
                    )
                }

                is GloblVar -> {
                    val varDecl = item.variable

                    // Rule 4: symbol not also declared as a function
                    if (varDecl.name in functionTable) {
                        error("'${varDecl.name}' declared as both a variable and a function")
                    }

                    // Rule 2: no duplicate global variable definition
                    if (varDecl.name !in globalVars || globalVars[varDecl.name] == false) {
                        globalVars[varDecl.name] = (varDecl.initializer != null)

                    } else error("Duplicate global variable '${varDecl.name}'")

                    // Rule 3: initializer must be a constant (or absent)
                    if (varDecl.initializer != null && varDecl.initializer !is IntegerLiteral) {
                        error("Global variable '${varDecl.name}' must be initialized with a constant value")
                    }
                }
            }
        }

        return Pair(functionTable, globalVars)
    }

    // -------- Phase 2: validate function bodies --------
    private fun validateFunctions(
        program: Program,
        functionTable: Map<String, FunctionInfo>,
        globalVars: Map<String, Boolean>
    ) {
        for (item in program.items) {
            if (item is FunDecl && item.function.body != null) {
                validateFunction(item.function, functionTable, globalVars)
            }
        }
    }

    private fun validateFunction(
        function: Function,
        functionTable: Map<String, FunctionInfo>,
        globalVars: Map<String, Boolean>
    ) {
        val paramScope = mutableSetOf<String>()
        for (p in function.parameters ?: emptyList()) {
            if (!paramScope.add(p)) {
                error("Duplicate parameter '$p' in function ${function.name}")
            }
        }

        // scope stack: each level holds variables declared in that block
        // function-level scope starts with parameters
        val scopeStack = ArrayDeque<MutableSet<String>>()
        scopeStack.addLast(paramScope)

        function.body?.forEach {
            validateBlockItem(it, scopeStack, functionTable, globalVars)
        }
    }

    private fun isVisible(
        name: String,
        scopeStack: ArrayDeque<MutableSet<String>>,
        globalVars: Map<String, Boolean>
    ): Boolean = name in globalVars || scopeStack.any { name in it }

    private fun validateBlockItem(
        item: BlockItem,
        scopeStack: ArrayDeque<MutableSet<String>>,
        functionTable: Map<String, FunctionInfo>,
        globalVars: Map<String, Boolean>
    ) {
        when (item) {
            is VarDeclaration -> {
                // Rule 1: validate initializer with currently visible variables (before declaring this one)
                item.initializer?.let { validateExpr(it, scopeStack, functionTable, globalVars) }

                // Rule 4: variable name can't be the same as a function name
//                if (item.name in functionTable) {
//                    error("Variable '${item.name}' conflicts with function name")
//                }

                // Rule 2: no duplicate declaration in the current (innermost) scope
                if (!scopeStack.last().add(item.name)) {
                    error("Duplicate variable '${item.name}' in the same scope")
                }
            }
            is Statement -> validateStatement(item, scopeStack, functionTable, globalVars)
        }
    }

    private fun validateStatement(
        stmt: Statement,
        scopeStack: ArrayDeque<MutableSet<String>>,
        functionTable: Map<String, FunctionInfo>,
        globalVars: Map<String, Boolean>
    ) {
        when (stmt) {
            is Return -> validateExpr(stmt.expr, scopeStack, functionTable, globalVars)
            is ExpStatement -> stmt.expr?.let { validateExpr(it, scopeStack, functionTable, globalVars) }
            is IfStatement -> {
                validateExpr(stmt.condition, scopeStack, functionTable, globalVars)
                validateStatement(stmt.thenBranch, scopeStack, functionTable, globalVars)
                stmt.elseBranch?.let { validateStatement(it, scopeStack, functionTable, globalVars) }
            }
            is Compound -> {
                scopeStack.addLast(mutableSetOf())
                stmt.body.forEach { validateBlockItem(it, scopeStack, functionTable, globalVars) }
                scopeStack.removeLast()
            }
            is WhileStatement -> {
                validateExpr(stmt.condition, scopeStack, functionTable, globalVars)
                validateStatement(stmt.body, scopeStack, functionTable, globalVars)
            }
            is DoWhileStatement -> {
                validateStatement(stmt.body, scopeStack, functionTable, globalVars)
                validateExpr(stmt.condition, scopeStack, functionTable, globalVars)
            }
            is ForStatement -> {
                scopeStack.addLast(mutableSetOf())
                stmt.initializer?.let { validateExpr(it, scopeStack, functionTable, globalVars) }
                validateExpr(stmt.condition, scopeStack, functionTable, globalVars)
                stmt.cycle?.let { validateExpr(it, scopeStack, functionTable, globalVars) }
                validateStatement(stmt.body, scopeStack, functionTable, globalVars)
                scopeStack.removeLast()
            }
            is ForDeclStatement -> {
                scopeStack.addLast(mutableSetOf())
                // Rule 1: validate initializer before adding the variable to scope
                stmt.initializer.initializer?.let { validateExpr(it, scopeStack, functionTable, globalVars) }
                // Rule 4 + Rule 2: check and register the for-loop variable
                if (stmt.initializer.name in functionTable) {
                    error("Variable '${stmt.initializer.name}' conflicts with function name")
                }
                if (!scopeStack.last().add(stmt.initializer.name)) {
                    error("Duplicate variable '${stmt.initializer.name}' in the same scope")
                }
                validateExpr(stmt.condition, scopeStack, functionTable, globalVars)
                stmt.cycle?.let { validateExpr(it, scopeStack, functionTable, globalVars) }
                validateStatement(stmt.body, scopeStack, functionTable, globalVars)
                scopeStack.removeLast()
            }
            is Break, is Continue -> {
                // intentionally ignored
            }
        }
    }

    private fun validateExpr(
        expr: Expression,
        scopeStack: ArrayDeque<MutableSet<String>>,
        functionTable: Map<String, FunctionInfo>,
        globalVars: Map<String, Boolean>
    ) {
        when (expr) {

            is Syscall -> {
                if (syscallCount > 1) error("Only one syscall allowed")
            }

            is FunctionCall -> {
                val info = functionTable[expr.name]
                    ?: error("Undefined function: ${expr.name}")

                if (expr.arguments.size != info.paramCount) {
                    error(
                        "Function ${expr.name} expects ${info.paramCount} args but got ${expr.arguments.size}"
                    )
                }

                if (expr.name in scopeStack.last()) {
                    error("Function ${expr.name} is shadowed by a local variable")
                }

                expr.arguments.forEach { validateExpr(it, scopeStack, functionTable, globalVars) }
            }

            is Variable -> {
                // Rule 1: variable must be declared before use
                if (!isVisible(expr.name, scopeStack, globalVars)) {
                    error("Undeclared variable '${expr.name}'")
                }
            }

            is Assignment -> {
                // Rule 1: variable being assigned must be declared
                if (!isVisible(expr.name, scopeStack, globalVars)) {
                    error("Undeclared variable '${expr.name}'")
                }
                validateExpr(expr.expr, scopeStack, functionTable, globalVars)
            }

            is Unary -> validateExpr(expr.expr, scopeStack, functionTable, globalVars)
            is Binary -> {
                validateExpr(expr.left, scopeStack, functionTable, globalVars)
                validateExpr(expr.right, scopeStack, functionTable, globalVars)
            }
            is Conditional -> {
                validateExpr(expr.condition, scopeStack, functionTable, globalVars)
                validateExpr(expr.thenBranch, scopeStack, functionTable, globalVars)
                validateExpr(expr.elseBranch, scopeStack, functionTable, globalVars)
            }
            is IntegerLiteral -> {}
        }
    }
}
