
// the sealed class is an abstract class with a protected constructor so:-
// 1. one can't create instances of it directly
// 2. thus its instances must be created by its subclasses
// 3. and since subclasses beyond the same pkg cannot access the protected constructor of token,
//      exhaustive 'when' statements can be used


sealed class Token {
    // Identifiers and Literals
    sealed class Keyword : Token() {
        data object Int : Keyword()
        data object Return : Keyword()
        data object If : Keyword()
        data object Else : Keyword()
        data object For : Keyword()
        data object While : Keyword()
        data object Do : Keyword()
        data object Break : Keyword()
        data object Continue : Keyword()
        data object Syscall : Keyword()
    }

    // assignment op
    data object Assign : Token()      // a = b

    // unary op
    data object BitComp : Token()     // ~a
    data object LogicNot : Token()    // !a

    // hybrid op (binary/unary)
    data object Minus : Token()       // -a or a - b

    // binary arithmetic op
    data object Plus : Token()       // a + b
    data object Mul : Token()        // a * b
    data object Div : Token()        // a / b
    data object Mod : Token()        // a % b

    // binary logical op
    data object And : Token()        // a && b
    data object Or : Token()         // a || b

    // binary relational op
    data object Eq : Token()         // a == b
    data object Neq : Token()        // a != b
    data object Lt : Token()         // a < b
    data object Lte : Token()        // a <= b
    data object Gt : Token()         // a > b
    data object Gte : Token()        // a >= b

    // binary bitwise
    data object BitAnd : Token()      // a & b
    data object BitOr : Token()       // a | b
    data object BitXor : Token()      // a ^ b
    data object LShift : Token()      // a << b
    data object RShift : Token()      // a >> b


    data class Identifier(val name: String) : Token()
    data class IntegerLiteral(val value: Int) : Token()

    // Punctuation
    data object OpenParen : Token()    // (
    data object CloseParen : Token()   // )
    data object OpenBrace : Token()    // {
    data object CloseBrace : Token()   // }
    data object Semicolon : Token()    // ;
    data object Comma : Token()        // ,

    // data classes can have multiple objects with different values,
    // whereas data objects are singletons i.e., one of a kind

    // ternary op                      // a ? b : c
    data object Question : Token()     // ?
    data object Colon : Token()        // :

    // hidden tokens
    data object EOF : Token()          // end of file
}