package compiler;

import codegen.CodeGen;

//import javax.swing.text.html.parser.Parser;
import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException {
        try {
            String inputFile = "G:\\Compiler\\decaf_compiler\\Phase1\\src\\compiler\\a.txt";
            String outputFile = "G:\\Compiler\\decaf_compiler\\Phase1\\src\\compiler\\b.text";
            File read = new File(inputFile);
            Writer writer = new FileWriter(outputFile);

            compile(read, writer);
            writer.flush();
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void compile(File read, Writer writer){ // for auto testing
        File fr = new File(String.valueOf(read));

        Scanner scanner = new Scanner(fr);
        parser parser = new parser(scanner);

        String errorCode = null;
        boolean isError = false;

        try {
            parser.parse();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            CodeGen.compile(parser.getRoot(), writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}