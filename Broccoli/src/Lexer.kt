import java.io.File

class Lexer {

    // why is int and return placed before identifier: so regex doesn't mistake them as that
    // similarly ordering matters for the other keywords and operators: e.g. == should be before =, <= before <, etc.
    private val regex = Regex(
        """
        (?<WHITESPACE>\s+)
        |(?<INT>int\b)
        |(?<RETURN>return\b)
        |(?<IF>if\b)
        |(?<ELSE>else\b)
        |(?<FOR>for\b)
        |(?<WHILE>while\b)
        |(?<DO>do\b)
        |(?<BREAK>break\b)
        |(?<CONTINUE>continue\b)
        |(?<SYSCALL>syscall\b)
        
        |(?<IDENTIFIER>[a-zA-Z_][a-zA-Z0-9_]*)
        |(?<INTEGER>\d+)
        
        |(?<AND>&&)
        |(?<OR>\|\|)
        
        |(?<INCREMENT>\+\+)
        |(?<DECREMENT>--)
        
        |(?<LSHIFT><<)
        |(?<RSHIFT>>>)
        |(?<BITAND>&)
        |(?<BITOR>\|)
        |(?<BITXOR>\^)
        
        |(?<EQ>==)
        |(?<NEQ>!=)
        |(?<LTE><=)
        |(?<GTE>>=)
        |(?<LT><)
        |(?<GT>>)
        
        |(?<ASSIGN>=)
        
        |(?<QUESTION>\?)
        |(?<COLON>:)
        
        |(?<PLUS>\+)
        |(?<MINUS>-)
        |(?<MUL>\*)
        |(?<DIV>/)
        |(?<MOD>%)
        
        |(?<BITCOMP>~)
        |(?<LOGICNOT>!)
        
        |(?<OPENPAREN>\()
        |(?<CLOSEPAREN>\))
        |(?<OPENBRACE>\{)
        |(?<CLOSEBRACE>\})
        |(?<SEMICOLON>;)
        |(?<COMMA>,)
        """.trimIndent(),
        RegexOption.COMMENTS
    )

    fun lex(file: File): List<Token> {

        val tokens = mutableListOf<Token>()
        var lineNumber = 0

        file.forEachLine { line ->
            lineNumber++
            var index = 0

            while (index < line.length) {

                val match = regex.matchAt(line, index)
                    ?: error("Invalid token at line $lineNumber, column ${index + 1}: '${line.drop(index)}'")

                val token = when {
                    match.groups["WHITESPACE"] != null -> null

                    match.groups["INT"] != null -> Token.Keyword.Int
                    match.groups["RETURN"] != null -> Token.Keyword.Return
                    match.groups["IF"] != null -> Token.Keyword.If
                    match.groups["ELSE"] != null -> Token.Keyword.Else
                    match.groups["FOR"] != null -> Token.Keyword.For
                    match.groups["WHILE"] != null -> Token.Keyword.While
                    match.groups["DO"] != null -> Token.Keyword.Do
                    match.groups["BREAK"] != null -> Token.Keyword.Break
                    match.groups["CONTINUE"] != null -> Token.Keyword.Continue
                    match.groups["SYSCALL"] !=null -> Token.Keyword.Syscall

                    match.groups["IDENTIFIER"] != null ->
                        Token.Identifier(match.value)

                    match.groups["INTEGER"] != null ->
                        Token.IntegerLiteral(match.value.toInt())

                    match.groups["AND"] != null -> Token.And
                    match.groups["OR"] != null -> Token.Or

                    match.groups["ASSIGN"] != null -> Token.Assign

                    match.groups["LSHIFT"] != null -> Token.LShift
                    match.groups["RSHIFT"] != null -> Token.RShift
                    match.groups["BITAND"] != null -> Token.BitAnd
                    match.groups["BITOR"] != null -> Token.BitOr
                    match.groups["BITXOR"] != null -> Token.BitXor

                    match.groups["EQ"] != null -> Token.Eq
                    match.groups["NEQ"] != null -> Token.Neq
                    match.groups["LT"] != null -> Token.Lt
                    match.groups["GT"] != null -> Token.Gt
                    match.groups["LTE"] != null -> Token.Lte
                    match.groups["GTE"] != null -> Token.Gte

                    match.groups["QUESTION"] != null -> Token.Question
                    match.groups["COLON"] != null -> Token.Colon

                    match.groups["PLUS"] != null -> Token.Plus
                    match.groups["MINUS"] != null -> Token.Minus
                    match.groups["MUL"] != null -> Token.Mul
                    match.groups["DIV"] != null -> Token.Div
                    match.groups["MOD"] != null -> Token.Mod

                    match.groups["BITCOMP"] != null -> Token.BitComp
                    match.groups["LOGICNOT"] != null -> Token.LogicNot

                    match.groups["OPENPAREN"] != null -> Token.OpenParen
                    match.groups["CLOSEPAREN"] != null -> Token.CloseParen
                    match.groups["OPENBRACE"] != null -> Token.OpenBrace
                    match.groups["CLOSEBRACE"] != null -> Token.CloseBrace
                    match.groups["SEMICOLON"] != null -> Token.Semicolon
                    match.groups["COMMA"] != null -> Token.Comma

                    else -> error("Unknown token at line $lineNumber, column $index")
                }

                if (token != null) tokens.add(token)

                index = match.range.last + 1
            }
        }

        //tokens.add(Token.EOF)
        return tokens
    }
}