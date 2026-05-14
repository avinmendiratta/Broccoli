
// this class hosts multiple func.s to parse diff. non-terminal symbols

class Parser(private val tokens: List<Token>) {

    var syscallCount = 0

    // to indicate the position of the cursor
    private var pos = 0

    private fun peek(): Token? = tokens.getOrNull(pos)

    private fun consume(): Token =
        tokens.getOrElse(pos++) { error("Unexpected end of input") }

    // for objects like /(
    private fun expect(expected: Token): Token {
        val token = consume()
        if (token != expected) {
            error("Expected $expected but got $token")
        }
        return token
    }

    // we need to write the function based on a generic type T
    // but generics are erased at runtime
    // so reified tells the compiler: “Keep the actual type of T available at runtime.”
    // reified only works on inline functions
    // the compiler replaces the function call with its body (inlining)
    // at each call site, it substitutes the actual type

    // expectType<Token.Identifier>()
    // this is how to use the function
    // for types like identifier
    private inline fun <reified T : Token> expectType(): T {
        val token = consume()
        if (token !is T) {
            error("Expected ${T::class.simpleName} but got $token")
        }
        return token
    }











    /*-------------------------------parsing non-terminals---------------------------------------*/

    private fun isAtEnd(): Boolean = pos >= tokens.size

    fun parse(): Program {
        val program = parseProgram()

        if (!isAtEnd()) {
            error("Unexpected tokens after program end")
        }

        return program
    }





    private fun parseProgram(): Program {
        val items = mutableListOf<TopItem>()

        while (!isAtEnd()) {
            when (peek()) {
                Token.Keyword.Int -> {
                    val eitem = parseTopItem()
                    items.add(eitem)
                }

                else -> error("Unexpected token at top level: ${peek()}")
            }
        }

        return Program(items)
    }

    private fun parseTopItem(): TopItem {
        expect(Token.Keyword.Int)
        val name = expectType<Token.Identifier>().name

        return when (peek()) {
            Token.OpenParen -> FunDecl(parseFunction(name))

            Token.Semicolon, Token.Assign -> {
                var initializer: Expression? = null

                if (peek() is Token.Assign) {
                    consume()
                    initializer = parseExpression()
                    require(initializer is IntegerLiteral) { "Global variable must be initialized with an integer" }
                }

                expect(Token.Semicolon)
                GloblVar(VarDeclaration(name, initializer))
            }

            else -> error("Expected '(' for function or ';'/'=' for global variable after '$name' but got ${peek()}")
        }
    }

    private fun parseFunction(name: String): Function {
//        expect(Token.Keyword.Int)
//
//        val name = expectType<Token.Identifier>().name

        expect(Token.OpenParen)

        val parameters = mutableListOf<String>()
        if (peek() !is Token.CloseParen) {
            do {
                expect(Token.Keyword.Int)
                val param = expectType<Token.Identifier>().name
                parameters.add(param)
            } while (peek() is Token.Comma && run { consume(); true })
        }

        expect(Token.CloseParen)

        return when (peek()) {
            Token.Semicolon -> {
                consume()
                Function(name, parameters, null)
            }

            Token.OpenBrace -> {
                consume()

                val items = mutableListOf<BlockItem>()
                while (peek() !is Token.CloseBrace && !isAtEnd()) {
                    items.add(parseBlockItem())
                }

                expect(Token.CloseBrace)
                Function(name, parameters, items)
            }

            else -> error("Expected ';' or '{' after function signature for '$name' but got ${peek()}")
        }

//        expect(Token.OpenBrace)
//
//        val items = mutableListOf<BlockItem>()
//        while (peek() !is Token.CloseBrace && !isAtEnd()) {
//            items.add(parseBlockItem())
//        }
//
//        expect(Token.CloseBrace)
//
//        return Function(name, parameters, items)
    }

    private fun parseBlockItem(): BlockItem {

        // this branch is actually parseVarDeclaration()
        if (peek() is Token.Keyword.Int) {
            consume()
            val variable = expectType<Token.Identifier>()
            var expr: Expression? = null

            if (peek() is Token.Assign) {
                consume()
                expr = parseExpression()
            }

            expect(Token.Semicolon)
            return VarDeclaration(variable.name, expr)

        } else return parseStatement()
    }

    private fun parseStatement(): Statement {
        return when (peek()) {

            is Token.Keyword.Return -> {
                consume()
                val expr = parseExpression()
                expect(Token.Semicolon)
                Return(expr)
            }

            is Token.Keyword.If -> {
                consume()
                expect(Token.OpenParen)
                val condition = parseExpression()
                expect(Token.CloseParen)

                // why am I not consuming an open brace here?
                // to allow then branch to be a compound statement
                // so that the scope of if block could be handled
                val thenBranch = parseStatement()
                var elseBranch: Statement? = null

                if (peek() is Token.Keyword.Else) {
                    consume()
                    elseBranch = parseStatement()
                }
                IfStatement(condition, thenBranch, elseBranch)
            }

            is Token.OpenBrace -> {
                consume()
                val items = mutableListOf<BlockItem>()

                while (peek() !is Token.CloseBrace && !isAtEnd()) {
                    items.add(parseBlockItem())
                }
                expect(Token.CloseBrace)
                Compound(items)
            }

            is Token.Keyword.While -> {
                consume()
                expect(Token.OpenParen)
                val condition = parseExpression()
                expect(Token.CloseParen)

                val body = parseStatement()

                WhileStatement(condition, body)
            }

            is Token.Keyword.Do -> {
                consume()

                val body = parseStatement()

                expect(Token.Keyword.While)
                expect(Token.OpenParen)
                val condition = parseExpression()
                expect(Token.CloseParen)
                expect(Token.Semicolon)

                DoWhileStatement(body, condition)
            }

            // also, I should lower for loops into while loops, do I do this in the parser or in codegen,
            // codegen is better since lowering involves taking initializer out of the loop,
            // but any declaration therein is a for local despite being outside the loop
            is Token.Keyword.For -> parseForStatement()

            is Token.Keyword.Break -> {
                consume()
                expect(Token.Semicolon)
                Break
            }

            is Token.Keyword.Continue -> {
                consume()
                expect(Token.Semicolon)
                Continue
            }

            is Token.Semicolon -> {
                consume()
                ExpStatement(null)
            }

            else -> {
                val expr = parseExpression()
                expect(Token.Semicolon)
                ExpStatement(expr)
            }
        }
    }

    private fun parseForStatement(): Statement {
        expect(Token.Keyword.For)
        expect(Token.OpenParen)

        // ---------- CASE 1: declaration ----------
        if (peek() is Token.Keyword.Int) {
            consume()
            val variable = expectType<Token.Identifier>()
            var initExpr: Expression? = null

            if (peek() is Token.Assign) {
                consume()
                initExpr = parseExpression()
            }
            expect(Token.Semicolon)

            val initializer = VarDeclaration(variable.name, initExpr)

            // condition and cycle
            val condition =
                if (peek() is Token.Semicolon) {
                    consume()
                    IntegerLiteral(1) // default true
                } else {
                    val cond = parseExpression()
                    expect(Token.Semicolon)
                    cond
                }
            val cycle =
                if (peek() is Token.CloseParen) null
                else parseExpression()
            expect(Token.CloseParen)
            val body = parseStatement()

            return ForDeclStatement(initializer, condition, cycle, body)
        }

        // ---------- CASE 2: expression or empty ----------
        val initializer: Expression? =
            if (peek() is Token.Semicolon) {
                consume()
                null
            } else {
                val expr = parseExpression()
                expect(Token.Semicolon)
                expr
            }

        // condition and cycle
        val condition =
            if (peek() is Token.Semicolon) {
                consume()
                IntegerLiteral(1) // default true
            } else {
                val cond = parseExpression()
                expect(Token.Semicolon)
                cond
            }
        val cycle =
            if (peek() is Token.CloseParen) null
            else parseExpression()
        expect(Token.CloseParen)
        val body = parseStatement()

        return ForStatement(initializer, condition, cycle, body)
    }

    private fun parseExpression(): Expression {

        if (peek() is Token.Keyword.Syscall) {
            consume()
            syscallCount++
            return Syscall
        }

        // this is actually correct,
        // we must allow identifiers to be part of Conditionals too
        val left = parseConditionalExpression()

        if (peek() is Token.Assign) {
            consume()

            if (left !is Variable) {
                error("Invalid assignment target")
            }

            val right = parseExpression() // right-associative
            return Assignment(left.name, right)
        }

        return left
    }

    private fun parseConditionalExpression(): Expression {
        val condition = parseLogicalOrExpression()

        if (peek() is Token.Question) {
            consume()
            val thenBranch = parseExpression()
            expect(Token.Colon)
            val elseBranch = parseConditionalExpression()

            return Conditional(condition, thenBranch, elseBranch)

        } else return condition
    }

    private fun parseLogicalOrExpression(): Expression {
        var expr = parseLogicalAndExpression()

        while (peek() is Token.Or) {
            consume()
            val op = BiOp.OR
            val right = parseLogicalAndExpression()
            expr = Binary(op, expr, right)
        }
        return expr
    }

    private fun parseLogicalAndExpression(): Expression {
        var expr = parseBitwiseOrExpression()

        while (peek() is Token.And) {
            consume()
            val op = BiOp.AND
            val right = parseBitwiseOrExpression()
            expr = Binary(op, expr, right)
        }
        return expr
    }

    private fun parseBitwiseOrExpression(): Expression {
        var expr = parseBitwiseXorExpression()

        while (peek() is Token.BitOr) {
            consume()
            val op = BiOp.BITOR
            val right = parseBitwiseXorExpression()
            expr = Binary(op, expr, right)
        }
        return expr
    }

    private fun parseBitwiseXorExpression(): Expression {
        var expr = parseBitwiseAndExpression()

        while (peek() is Token.BitXor) {
            consume()
            val op = BiOp.BITXOR
            val right = parseBitwiseAndExpression()
            expr = Binary(op, expr, right)
        }
        return expr
    }

    private fun parseBitwiseAndExpression(): Expression {
        var expr = parseEqualityExpression()

        while (peek() is Token.BitAnd) {
            consume()
            val op = BiOp.BITAND
            val right = parseEqualityExpression()
            expr = Binary(op, expr, right)
        }
        return expr
    }

    private fun parseEqualityExpression(): Expression {
        var expr = parseRelationalExpression()

        while (peek() is Token.Eq || peek() is Token.Neq) {
            val token = consume()
            val op = if (token is Token.Eq) BiOp.EQ
                     else BiOp.NEQ

            val right = parseRelationalExpression()
            expr = Binary(op, expr, right)
        }
        return expr
    }

    private fun parseRelationalExpression(): Expression {
        var expr = parseShiftExpression()

        while (peek() is Token.Lt || peek() is Token.Lte || peek() is Token.Gt || peek() is Token.Gte) {
            val token = consume()
            val op = when (token) {
                is Token.Lt -> BiOp.LT
                is Token.Lte -> BiOp.LTE
                is Token.Gt -> BiOp.GT
                is Token.Gte -> BiOp.GTE
                else -> error("Unexpected token")
            }

            val right = parseShiftExpression()
            expr = Binary(op, expr, right)
        }

        return expr
    }

    private fun parseShiftExpression(): Expression {
        var expr = parseAdditiveExpression()

        while (peek() is Token.LShift || peek() is Token.RShift) {
            val token = consume()
            val op = if (token is Token.LShift) BiOp.LSHIFT
                     else BiOp.RSHIFT

            val right = parseAdditiveExpression()
            expr = Binary(op, expr, right)
        }
        return expr
    }

    private fun parseAdditiveExpression(): Expression {
        var expr = parseTerm()

        while (peek() is Token.Plus || peek() is Token.Minus) {
            val token = consume()
            val op = if (token is Token.Plus) BiOp.PLUS
                     else BiOp.MINUS

            val right = parseTerm()
            expr = Binary(op, expr, right)
        }

        return expr
    }

    private fun parseTerm(): Expression {
        var factor = parseFactor()

        while (peek() is Token.Mul || peek() is Token.Div || peek() is Token.Mod) {
            val token = consume()
            val op = when (token) {
                is Token.Mul -> BiOp.MUL
                is Token.Div -> BiOp.DIV
                is Token.Mod -> BiOp.MOD
                else -> error("Unexpected token")
            }

            val right = parseFactor()
            factor = Binary(op, factor, right)
        }

        return factor
    }

    private fun parseFactor(): Expression {
        return when (val token = peek()) {
            is Token.OpenParen -> {
                consume()
                val exp = parseExpression()
                expect(Token.CloseParen)
                exp
            }
            is Token.Minus -> {
                consume()
                Unary(UnanOp.MINUS, parseFactor())
            }
            is Token.BitComp -> {
                consume()
                Unary(UnanOp.BITCOMP, parseFactor())
            }
            is Token.LogicNot -> {
                consume()
                Unary(UnanOp.LOGICNOT, parseFactor())
            }
            is Token.IntegerLiteral -> {
                // since consume() returns token which as such don't have value property
                // so we typecast consume()
                val literal = consume() as Token.IntegerLiteral
                IntegerLiteral(literal.value)
            }
            is Token.Identifier -> {
                val name = (consume() as Token.Identifier).name

                if (peek() is Token.OpenParen) {
                    consume() // (

                    val args = mutableListOf<Expression>()

                    if (peek() !is Token.CloseParen) {
                        do {
                            args.add(parseExpression())
                        } while (peek() is Token.Comma && run { consume(); true })
                    }

                    expect(Token.CloseParen)
                    FunctionCall(name, args)
                } else {
                    Variable(name)
                }
            }
            else -> error("Unexpected token: $token")
        }
    }
}