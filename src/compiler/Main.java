package compiler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;


public class Main {
    public static String run(java.io.File inputFile) throws Exception {
        Scanner s = new Scanner();
        String input = Files.readString(inputFile.toPath(), StandardCharsets.US_ASCII);
        return String.join("\n", s.filterTokens(input));
    }
}