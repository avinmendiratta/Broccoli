import java.io.File

fun main(args: Array<String>) {
    require(args.isNotEmpty()) { "Usage: compiler <input.c>" }

    require(args[0].endsWith(".c")) { "Input file must have a .c extension: ${args[0]}" }
    val fileName = args[0].removeSuffix(".c")

    val inputFile = File(args[0])
    require(inputFile.exists()) { "Input file does not exist: ${args[0]}" }

    val outputPath = "$fileName.s"

    val tokens = Lexer().lex(inputFile)

    val parser = Parser(tokens)
    val program = parser.parse()

    println(program.prettyPrint())

    val inspector = Inspector()
    inspector.syscallCount = parser.syscallCount
    inspector.validate(program)

    val gen = Generator()
    gen.syscallCount = parser.syscallCount
    gen.codegen(program, outputPath)

    println("Stack Usage by the constituent functions:")

    gen.stackUsage.forEach { (function, i) -> println("$function: $i") }

}