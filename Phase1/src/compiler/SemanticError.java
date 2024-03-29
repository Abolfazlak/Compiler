package compiler;

import java.io.FileWriter;
import java.io.IOException;

public class SemanticError extends Exception {
    public SemanticError(String message) throws IOException, SemanticError {
        String assemblyCode =
                ".data\n" +
                        "\tnewLine: \t.asciiz \t\"\\n\"\n" +
                        "\tbool_0: \t.asciiz \t\"false\"\n" +
                        "\tbool_1: \t.asciiz \t\"true\"\n" +
                        "\tzeroDouble: \t.double \t0.0\n" +
                        "\tmainScope_StringLiteral0:\t.asciiz\t\"Semantic Error\"\n" +
                        ".text\n" +
                        "\tldc1\t$f0, zeroDouble\n" +
                        "\tjal\tmain\n" +
                        "PrintBool:\n" +
                        "\tbeq\t$a0, 0, Print_Bool0\n" +
                        "\tla\t$v1, bool_1\n" +
                        "\tb\tPrint_Bool1\n" +
                        "Print_Bool0:\n" +
                        "\tla\t$v1, bool_0\n" +
                        "Print_Bool1:\n" +
                        "\tjr\t$ra\n" +
                        "main:\n" +
                        "\taddi\t$sp, $sp, -16\n" +
                        "\tsw\t$a0, 0($sp)\n" +
                        "\tsw\t$a1, 4($sp)\n" +
                        "\tsw\t$a2, 8($sp)\n" +
                        "\tsw\t$a3, 12($sp)\n" +
                        "\tla\t$v1, mainScope_StringLiteral0\n" +
                        "\tli\t$v0, 4\n" +
                        "\tmove\t$a0, $v1\n" +
                        "\tsyscall\n" +
                        "\t# This line is going to signal end of program.\n" +
                        "\tli\t$v0, 10\n" +
                        "\tsyscall\n";
        FileWriter out = new FileWriter("tests/out.asm");
        out.write(assemblyCode);
        out.close();
        throw new SemanticError(message, this.fillInStackTrace());
    }


    public SemanticError(String message, Throwable fillInStackTrace) {
        super(message, fillInStackTrace);
    }

    public static String writeError() {

        return ".data\n" +
                "\tnewLine: \t.asciiz \t\"\\n\"\n" +
                "\tbool_0: \t.asciiz \t\"false\"\n" +
                "\tbool_1: \t.asciiz \t\"true\"\n" +
                "\tzeroDouble: \t.double \t0.0\n" +
                "\tmainScope_StringLiteral0:\t.asciiz\t\"Semantic Error\"\n" +
                ".text\n" +
                "\tldc1\t$f0, zeroDouble\n" +
                "\tjal\tmain\n" +
                "PrintBool:\n" +
                "\tbeq\t$a0, 0, Print_Bool0\n" +
                "\tla\t$v1, bool_1\n" +
                "\tb\tPrint_Bool1\n" +
                "Print_Bool0:\n" +
                "\tla\t$v1, bool_0\n" +
                "Print_Bool1:\n" +
                "\tjr\t$ra\n" +
                "main:\n" +
                "\taddi\t$sp, $sp, -16\n" +
                "\tsw\t$a0, 0($sp)\n" +
                "\tsw\t$a1, 4($sp)\n" +
                "\tsw\t$a2, 8($sp)\n" +
                "\tsw\t$a3, 12($sp)\n" +
                "\tla\t$v1, mainScope_StringLiteral0\n" +
                "\tli\t$v0, 4\n" +
                "\tmove\t$a0, $v1\n" +
                "\tsyscall\n" +
                "\t# This line is going to signal end of program.\n" +
                "\tli\t$v0, 10\n" +
                "\tsyscall\n";
    }

}
