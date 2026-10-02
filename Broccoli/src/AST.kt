

// println(program.prettyPrint())
// to print the AST for debugging

fun AST.prettyPrint(): String = buildString {
        appendNode(this@prettyPrint, 0)
}

private fun StringBuilder.line(indent: Int, text: String) {
    append("  ".repeat(indent))
    appendLine(text)
}

private fun StringBuilder.appendNode(node: AST, indent: Int) {
    when (node) {
        is Program -> {
            line(indent, "Program")
            node.items.forEach { appendNode(it, indent + 1) }
        }

        is FunDecl -> appendNode(node.function, indent)

        is GloblVar -> appendNode(node.variable, indent)

        is Function -> {
            line(indent, "Function(name=${node.name})")
            node.parameters?.forEach { line(indent + 1, "Parameter($it)") }
            node.body?.forEach { appendNode(it, indent + 1) }
        }

        is VarDeclaration -> {
            line(indent, "VarDeclaration(name=${node.name}, initializer=${node.initializer})")
            if (node.initializer != null) appendNode(node.initializer, indent + 1)
        }

        is Return -> {
            line(indent, "Return")
            appendNode(node.expr, indent + 1)
        }

        is ExpStatement -> {
            line(indent, "ExpStatement")
            if (node.expr == null) {
                line(indent + 1, "null expression")
            } else appendNode(node.expr, indent + 1)

        }

        is IfStatement -> {
            line(indent, "IfStatement")

            line(indent + 1, "Condition")
            appendNode(node.condition, indent + 2)

            line(indent + 1, "Then")
            appendNode(node.thenBranch, indent + 2)

            if (node.elseBranch != null) {
                line(indent + 1, "Else")
                appendNode(node.elseBranch, indent + 2)
            }
        }

        is ForStatement -> {
            line(indent, "ForStatement")

            line(indent + 1, "Initializer")
            if (node.initializer != null)
                appendNode(node.initializer, indent + 2)
            else line(indent + 2, "null")

            line(indent + 1, "Condition")
            appendNode(node.condition, indent + 2)

            line(indent + 1, "Cycle")
            if (node.cycle != null)
                appendNode(node.cycle, indent + 2)
            else line(indent + 2, "null")

            line(indent + 1, "Body")
            appendNode(node.body, indent + 2)
        }

        is ForDeclStatement -> {
            line(indent, "ForDeclStatement")

            line(indent + 1, "Initializer")
            appendNode(node.initializer, indent + 2)

            line(indent + 1, "Condition")
            appendNode(node.condition, indent + 2)

            line(indent + 1, "Cycle")
            if (node.cycle != null)
                appendNode(node.cycle, indent + 2)
            else line(indent + 2, "null")

            line(indent + 1, "Body")
            appendNode(node.body, indent + 2)

        }

        is WhileStatement -> {
            line(indent, "WhileStatement")
            line(indent + 1, "Condition")
            appendNode(node.condition, indent + 2)
            line(indent + 1, "Body")
            appendNode(node.body, indent + 2)
        }

        is DoWhileStatement -> {
            line(indent, "DoWhileStatement")
            line(indent + 1, "Body")
            appendNode(node.body, indent + 2)
            line(indent + 1, "Condition")
            appendNode(node.condition, indent + 2)
        }

        is Break ->
            line(indent, "Break")

        is Continue ->
            line(indent, "Continue")

        is Compound -> {
            line(indent, "Compound")
            node.body.forEach { item -> appendNode(item, indent + 1) }
        }

        is Syscall -> {
            line(indent, "Syscall")
            line(indent + 1, "Don ko pakadna mushkil hi nahi, namumkin hai!")
        }

        is FunctionCall -> {
            line(indent, "FunctionCall(name=${node.name})")
            node.arguments.forEach { appendNode(it, indent + 1) }
        }

        is IntegerLiteral -> {
            line(indent, "IntegerLiteral(${node.value})")
        }

        is Variable -> {
            line(indent, "Variable(${node.name})")
        }

        is Assignment -> {
            line(indent, "Assignment(name=${node.name})")
            appendNode(node.expr, indent + 1)
        }

        is Unary -> {
            line(indent, "Unary(${node.operator})")
            appendNode(node.expr, indent + 1)
        }
        
        is Binary -> {
            line(indent, "Binary(${node.operator})")
            appendNode(node.left, indent + 1)
            appendNode(node.right, indent + 1)
        }

        is Conditional -> {
            line(indent, "Conditional")

            line(indent + 1, "Condition")
            appendNode(node.condition, indent + 2)

            line(indent + 1, "Then")
            appendNode(node.thenBranch, indent + 2)

            line(indent + 1, "Else")
            appendNode(node.elseBranch, indent + 2)
        }
    }
}










/*----------------------------------------AST-----------------------------------------------*/

sealed class AST

    data class Program(val items: List<TopItem>) : AST()

    sealed class TopItem : AST()
        data class FunDecl(val function: Function) : TopItem()
        data class GloblVar(val variable: VarDeclaration) : TopItem()

    data class Function(
        val name: String,
        val parameters: List<String>?,
        val body: List<BlockItem>? = emptyList()
    ) : AST()



    sealed class BlockItem : AST()

        data class VarDeclaration(
            val name: String,
            val initializer: Expression?
        ) : BlockItem()

        sealed class Statement : BlockItem()

            data class Return(val expr: Expression) : Statement()

            data class ExpStatement(val expr: Expression?) : Statement()

            data class IfStatement(
                val condition: Expression,
                val thenBranch: Statement,
                val elseBranch: Statement?
            ) : Statement()

            // A compound statement(block) is just a list of statements and declarations
            data class Compound(val body: List<BlockItem>) : Statement()

            data class ForStatement(
                val initializer: Expression?,
                val condition: Expression,
                val cycle: Expression?,
                val body: Statement
            ) : Statement()

            data class ForDeclStatement(
                val initializer: VarDeclaration,
                val condition: Expression,
                val cycle: Expression?,
                val body: Statement
            ) : Statement()

            data class WhileStatement(
                val condition: Expression,
                val body: Statement
            ) : Statement()

            data class DoWhileStatement(
                val body: Statement,
                val condition: Expression
            ) : Statement()

            data object Break : Statement()
            data object Continue : Statement()



    sealed class Expression : AST()

        data object Syscall : Expression()

        data class FunctionCall(
            val name: String,
            val arguments: List<Expression> = emptyList()
        ) : Expression()

        data class IntegerLiteral(val value: Int) : Expression()

        data class Variable(val name: String) : Expression()         // to reference a variable, basically to get its value as an expression

        data class Assignment(                                       //string is variable, exp is value to assign
            val name: String,
            val expr: Expression
        ) : Expression()

        data class Unary(
            val operator: UnanOp,
            val expr: Expression
        ) : Expression()

        data class Binary(
            val operator: BiOp,
            val left: Expression,
            val right: Expression
        ) : Expression()

        data class Conditional(
            val condition: Expression,
            val thenBranch: Expression,
            val elseBranch: Expression
        ) : Expression()



enum class UnanOp {
    MINUS,        // -
    BITCOMP,      // ~
    LOGICNOT      // !
}
enum class BiOp {

    // bitwise
    BITAND,      // &
    BITOR,       // |
    BITXOR,      // ^
    LSHIFT,      // <<
    RSHIFT,      // >>

    // logical
    AND,         // &&
    OR,          // ||

    // equality
    EQ,          // ==
    NEQ,         // !=

    // relational
    LT,          // <
    LTE,         // <=
    GT,          // >
    GTE,         // >=

    // arithmetic
    PLUS,        // +
    MINUS,       // -
    MUL,         // *
    DIV,         // /
    MOD          // %
}