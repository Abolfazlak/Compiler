package compiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java_cup.runtime.*;

public class Scanner {
    public String[] tokens;
    public String[] finalTokens;
    public static List<String> defineToken = new ArrayList<>();

    SymbolFactory symbolFactory;
    public Scanner(File input){
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(input))) {

            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                sb.append(sCurrentLine).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        tokens = filterTokens(sb.toString());
        finalTokens = new String[tokens.length + defineToken.size()];

        for (int i = 0; i < defineToken.size(); i++){
            finalTokens[i] = defineToken.get(i);
        }
        System.arraycopy(tokens, 0, finalTokens, defineToken.size(), defineToken.size() + tokens.length - defineToken.size());
        symbolFactory = new DefaultSymbolFactory();
    }

    Pattern _special = Pattern.compile("^\\b(?:__func__|__line__|bool|break|btoi|class|continue|define|double|dtoi|else|for|if|import|int|itob|itod|new|NewArray|null|Print|private|public|ReadInteger|ReadLine|return|string|this|void|while)\\b");
    Pattern _bool = Pattern.compile("^\\b(true|false)\\b");
    Pattern _id = Pattern.compile("^\\b([a-zA-Z]+\\w*)\\b");
    Pattern _int10 = Pattern.compile("^(\\d+)");
    Pattern _int16 = Pattern.compile("^(0[xX][A-Fa-f0-9]+)");
    Pattern _double = Pattern.compile("^(\\d+\\.\\d*)");
    Pattern _SN = Pattern.compile("^(\\d+\\.\\d*[eE][+-]?\\d+)");
    Pattern _operators = Pattern.compile("^((\\+=?)|(-=?)|(\\*=?)|(/=?)|%|<=?|>=?|==?|!=?|(&&)|(\\|\\|)|;|,|(\\.)|(\\[\\s*])|(\\[)|(])|(\\()|(\\))|(\\{)|(}))");
    Pattern _string = Pattern.compile("^(\"((\\\\\")*.*?(\\\\\")*)*(.*?)((\\\\\")*.*?(\\\\\")*)*\")");
    Pattern fallback = Pattern.compile("[\\s\\S]");
    Pattern startedUnderline = Pattern.compile("^(_[a-zA-Z_]+)");

    Map<TokenType, Pattern> tokenMap = new LinkedHashMap<>();

    public List<String> getTokens(String input) {
        PreProcessor preProcessor = new PreProcessor();
        List<String> tokenList = new ArrayList<>();

        String removedComments = preProcessor.removeComments(input);
        String replacedDefine = preProcessor.replaceDefine(removedComments);

        addPatternsToMap();

        int cursor = 0;
        while (cursor < replacedDefine.length()) {
            for (Map.Entry<TokenType, Pattern> entry :
                    tokenMap.entrySet()) {
                Matcher matcher = entry.getValue().matcher(replacedDefine);
                matcher.region(cursor, replacedDefine.length());
                if (matcher.find()) {
                    tokenList.add(entry.getKey()+" "+matcher.group());
                    cursor += matcher.group().length();
                    break;
                }
            }
        }
        return tokenList;
    }

    public String[] filterTokens(String input) {
        List<String> tokens = getTokens(input);

        tokens.removeIf(entry -> entry.contains(TokenType.T_UNKNOWN.name()));
        tokens.removeIf(entry -> entry.contains(TokenType.T_UNDERLINE.name()));

        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).contains("T_SPECIAL")){
                tokens.set(i, tokens.get(i).replace("T_SPECIAL ", ""));
            }
            if (tokens.get(i).contains("T_OPERATORS")){
                tokens.set(i, tokens.get(i).replace("T_OPERATORS ", ""));
                tokens.set(i, tokens.get(i).replaceAll("\\[\\s*]", "\\[]"));
            }
            if (tokens.get(i).contains("T_INT10LITERAL")){
                tokens.set(i, tokens.get(i).replace("T_INT10LITERAL", "T_INTLITERAL"));
            }
            if (tokens.get(i).contains("T_INT16LITERAL")){
                tokens.set(i, tokens.get(i).replace("T_INT16LITERAL", "T_INTLITERAL"));
            }
            if (tokens.get(i).contains("T_DOUBLELITERALSN")){
                tokens.set(i, tokens.get(i).replace("T_DOUBLELITERALSN", "T_DOUBLELITERAL"));
            }
        }
        String[] out = new String[tokens.size()];
        out = tokens.toArray(out);
        return out;
    }

    public void addPatternToMap (TokenType tokenType, Pattern pattern, Map<TokenType, Pattern> map){
        map.put(tokenType, pattern);
    }
    public void addPatternsToMap(){
        addPatternToMap(TokenType.T_STRINGLITERAL, _string, tokenMap);
        addPatternToMap(TokenType.T_SPECIAL, _special, tokenMap);
        addPatternToMap(TokenType.T_BOOLEANLITERAL, _bool, tokenMap);
        addPatternToMap(TokenType.T_UNDERLINE, startedUnderline, tokenMap);
        addPatternToMap(TokenType.T_ID, _id, tokenMap);
        addPatternToMap(TokenType.T_DOUBLELITERALSN, _SN, tokenMap);
        addPatternToMap(TokenType.T_OPERATORS, _operators, tokenMap);
        addPatternToMap(TokenType.T_DOUBLELITERAL, _double, tokenMap);
        addPatternToMap(TokenType.T_INT16LITERAL, _int16, tokenMap);
        addPatternToMap(TokenType.T_INT10LITERAL, _int10, tokenMap);
        addPatternToMap(TokenType.T_UNKNOWN, fallback, tokenMap);
    }

    int index = 0;
    public Symbol next_token(){
        if (index == finalTokens.length)
            return symbolFactory.newSymbol("EOF", sym.EOF);
        String[] splitTokens = finalTokens[index++].split(" ");
        switch (splitTokens[0]) {
            case "=" : return symbolFactory.newSymbol("EQUAL", sym.EQUAL);
            case ">" : return symbolFactory.newSymbol("GREATER", sym.GREATER);
            case "<" : return symbolFactory.newSymbol("LESS", sym.LESS);
            case ">=" : return symbolFactory.newSymbol("GREATEREQUAL", sym.GREATEREQUAL);
            case "<=" : return symbolFactory.newSymbol("LESSEQUAL", sym.LESSEQUAL);
            case "==" : return symbolFactory.newSymbol("EQUALEQUAl", sym.EQUALEQUAL);
            case "!=" : return symbolFactory.newSymbol("NOTEQUAl", sym.NOTEQUAL);
            case "!" : return symbolFactory.newSymbol("NOT", sym.NOT);
            case "&&" : return symbolFactory.newSymbol("AND", sym.AND);
            case "||" : return symbolFactory.newSymbol("OR", sym.OR);
            case "%" : return symbolFactory.newSymbol("MOD", sym.MOD);
            case "+" : return symbolFactory.newSymbol("PLUS", sym.PLUS);
            case "-" : return symbolFactory.newSymbol("MINUS", sym.MINUS);
            case "*" : return symbolFactory.newSymbol("MULTIPLY", sym.MULTIPLY);
            case "/" : return symbolFactory.newSymbol("DIVIDE", sym.DIVIDE);
            case "+=" : return symbolFactory.newSymbol("ADDITIONASSIGNMENT", sym.ADDITIONASSIGNMENT);
            case "-=" : return symbolFactory.newSymbol("SUBTRACTIONASSIGNMENT", sym.SUBTRACTIONASSIGNMENT);
            case "*=" : return symbolFactory.newSymbol("MULTIPLICATIONASSIGNMENT", sym.MULTIPLICATIONASSIGNMENT);
            case "/=" : return symbolFactory.newSymbol("AUGMENTEDASSIGNMENT", sym.AUGMENTEDASSIGNMENT);
            case "(" : return symbolFactory.newSymbol("LEFTPARENTHESES", sym.LEFTPARENTHESES);
            case ")" : return symbolFactory.newSymbol("RIGHTPARENTHESES", sym.RIGHTPARENTHESES);
            case "{" : return symbolFactory.newSymbol("LEFTCURLYBRACKET", sym.LEFTCURLYBRACKET);
            case "}" : return symbolFactory.newSymbol("RIGHTCURLYBRACKET", sym.RIGHTCURLYBRACKET);
            case "[" : return symbolFactory.newSymbol("LEFTSQUAREBRACKET", sym.LEFTSQUAREBRACKET);
            case "]" : return symbolFactory.newSymbol("RIGHTSQUAREBRACKET", sym.RIGHTSQUAREBRACKET);
            case "[]" : return symbolFactory.newSymbol("LEFTRIGHTSQUAREBRACKET", sym.LEFTRIGHTSQUAREBRACKET);
            case "." : return symbolFactory.newSymbol("DOT", sym.DOT);
            case "," : return symbolFactory.newSymbol("COMMA", sym.COMMA);
            case ";" : return symbolFactory.newSymbol("SEMI", sym.SEMI);
            case "int" : return symbolFactory.newSymbol("INT", sym.INT);
            case "double" : return symbolFactory.newSymbol("DOUBLE", sym.DOUBLE);
            case "bool" : return symbolFactory.newSymbol("BOOL", sym.BOOL);
            case "string" : return symbolFactory.newSymbol("STRING", sym.STRING);
            case "define" : return symbolFactory.newSymbol("DEFINE", sym.DEFINE);
            case "DEFINESTMT": return symbolFactory.newSymbol("DEFINESTMT", sym.DEFINESTMT);
            case "import" : return symbolFactory.newSymbol("IMPORT", sym.IMPORT);
            case "void" : return symbolFactory.newSymbol("VOID", sym.VOID);
            case "class" : return symbolFactory.newSymbol("CLASS", sym.CLASS);
            case "public" : return symbolFactory.newSymbol("PUBLIC", sym.PUBLIC);
            case "private" : return symbolFactory.newSymbol("PRIVATE", sym.PRIVATE);
            case "if" : return symbolFactory.newSymbol("IF", sym.IF);
            case "else" : return symbolFactory.newSymbol("ELSE", sym.ELSE);
            case "for" : return symbolFactory.newSymbol("FOR", sym.FOR);
            case "while" : return symbolFactory.newSymbol("WHILE", sym.WHILE);
            case "return" : return symbolFactory.newSymbol("RETURN", sym.RETURN);
            case "break" : return symbolFactory.newSymbol("BREAK", sym.BREAK);
            case "continue" : return symbolFactory.newSymbol("CONTINUE", sym.CONTINUE);
            case "this" : return symbolFactory.newSymbol("THIS", sym.THIS);
            case "new" : return symbolFactory.newSymbol("NEW", sym.NEW);
            case "ReadInteger" : return symbolFactory.newSymbol("READINTEGER", sym.READINTEGER);
            case "ReadLine" : return symbolFactory.newSymbol("READLINE", sym.READLINE);
            case "__line__" : return symbolFactory.newSymbol("LINE", sym.LINE);
            case "__func__" : return symbolFactory.newSymbol("FUNC", sym.FUNC);
            case "NewArray" : return symbolFactory.newSymbol("NEWARRAY", sym.NEWARRAY);
            case "Print": return symbolFactory.newSymbol("PRINT", sym.PRINT);
            case "itod" : return symbolFactory.newSymbol("ITOD", sym.ITOD);
            case "itob" : return symbolFactory.newSymbol("ITOB", sym.ITOB);
            case "btoi" : return symbolFactory.newSymbol("BTOI", sym.BTOI);
            case "dtoi" : return symbolFactory.newSymbol("DTOI", sym.DTOI);
            case "line" : return symbolFactory.newSymbol("LINE", sym.LINE);
            case "func" : return symbolFactory.newSymbol("FUNC", sym.FUNC);
            case "null" : return symbolFactory.newSymbol("NULL", sym.NULL);
            case "T_STRINGLITERAL" : return symbolFactory.newSymbol("stringConstant", sym.stringConstant, splitTokens[1]);
            case "T_BOOLEANLITERAL" : return symbolFactory.newSymbol("boolConstant", sym.boolConstant, splitTokens[1]);
            case "T_ID" : return symbolFactory.newSymbol("ID", sym.ID, splitTokens[1]);
            case "T_INTLITERAL" : return symbolFactory.newSymbol("intConstant", sym.intConstant, splitTokens[1]);
            case "T_DOUBLELITERAL" : return symbolFactory.newSymbol("doubleConstant", sym.doubleConstant, splitTokens[1]);
            default : return symbolFactory.newSymbol("EOF", sym.EOF);
        }
    }

}
    enum TokenType {
        T_STRINGLITERAL,
        T_BOOLEANLITERAL,
        T_ID,
        T_INT10LITERAL,
        T_INT16LITERAL,
        T_DOUBLELITERAL,
        T_DOUBLELITERALSN,
        T_OPERATORS,
        T_SPECIAL,
        T_UNKNOWN,
        T_UNDERLINE
    }
