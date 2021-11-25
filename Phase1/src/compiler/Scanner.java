package compiler;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java_cup.runtime.*;

public class Scanner {
    public String[] tokens;
    SymbolFactory symbolFactory;
    public Scanner(String input){
        tokens = filterTokens(input);
        symbolFactory = new DefaultSymbolFactory();
    }

    Pattern _special = Pattern.compile("^\\b(?:__func__|__line__|bool|break|btoi|class|continue|define|double|dtoi|else|for|if|import|int|itob|itod|new|NewArray|null|Print|private|public|ReadInteger|ReadLine|return|string|this|void|while)\\b");
    Pattern _bool = Pattern.compile("^\\b(true|false)\\b");
    Pattern _id = Pattern.compile("^\\b([a-zA-Z]+\\w*)\\b");
    Pattern _int10 = Pattern.compile("^(\\d+)");
    Pattern _int16 = Pattern.compile("^(0[xX][A-Fa-f0-9]+)");
    Pattern _double = Pattern.compile("^(\\d+\\.\\d*)");
    Pattern _SN = Pattern.compile("^(\\d+\\.\\d*[eE][+-]?\\d+)");
    Pattern _operators = Pattern.compile("^((\\+=?)|(-=?)|(\\*=?)|(/=?)|%|<=?|>=?|==?|!=?|(&&)|(\\|\\|)|;|,|(\\.)|(\\[)|(])|(\\()|(\\))|(\\{)|(}))");
    Pattern _string = Pattern.compile("^(\"((\\\\\")*.*?(\\\\\")*)*(.*?)((\\\\\")*.*?(\\\\\")*)*\")");
    Pattern fallback = Pattern.compile("[\\s\\S]");

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

        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).contains("T_SPECIAL")){
                tokens.set(i, tokens.get(i).replace("T_SPECIAL ", ""));
            }
            if (tokens.get(i).contains("T_OPERATORS")){
                tokens.set(i, tokens.get(i).replace("T_OPERATORS ", ""));
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
        if (index == tokens.length)
            return symbolFactory.newSymbol("EOF", sym.EOF);
        String[] splitTokens = tokens[index++].split(" ");
        return switch (splitTokens[0]) {
            case "=" -> symbolFactory.newSymbol("EQUAL", sym.EQUAL);
            case ">" -> symbolFactory.newSymbol("GREATER", sym.GREATER);
            case "<" -> symbolFactory.newSymbol("LESS", sym.LESS);
            case ">=" -> symbolFactory.newSymbol("GREATEREQUAL", sym.GREATEREQUAL);
            case "<=" -> symbolFactory.newSymbol("LESSEQUAL", sym.LESSEQUAL);
            case "==" -> symbolFactory.newSymbol("EQUALEQUAl", sym.EQUALEQUAl);
            case "!=" -> symbolFactory.newSymbol("NOTEQUAl", sym.NOTEQUAl);
            case "!" -> symbolFactory.newSymbol("NOT", sym.NOT);
            case "&&" -> symbolFactory.newSymbol("AND", sym.AND);
            case "||" -> symbolFactory.newSymbol("OR", sym.OR);
            case "%" -> symbolFactory.newSymbol("MOD", sym.MOD);
            case "+" -> symbolFactory.newSymbol("PLUS", sym.PLUS);
            case "-" -> symbolFactory.newSymbol("MINUS", sym.MINUS);
            case "*" -> symbolFactory.newSymbol("MULTIPLY", sym.MULTIPLY);
            case "/" -> symbolFactory.newSymbol("DIVIDE", sym.DEVIDE);
            case "+=" -> symbolFactory.newSymbol("ADDITIONASSIGNMENT", sym.ADDITIONASSIGNEMENT);
            case "-=" -> symbolFactory.newSymbol("SUBTRACTIONASSIGNMENT", sym.SUBTRACTIONASSIGNMENT);
            case "*=" -> symbolFactory.newSymbol("MULTIPLICATIONASSIGNMENT", sym.MULTIPLICATIONASSIGNMENT);
            case "/=" -> symbolFactory.newSymbol("AUGMENTEDASSIGNMENT", sym.AUGMENTEDASSIGNMENT);
            case "(" -> symbolFactory.newSymbol("LEFTPARENTHESES", sym.LEFTPARENTHESES);
            case ")" -> symbolFactory.newSymbol("RIGHTPARENTHESES", sym.RIGHTPARENTHESES);
            case "{" -> symbolFactory.newSymbol("LEFTCURLYBRACKET", sym.LEFTCURLYBRACKET);
            case "}" -> symbolFactory.newSymbol("RIGHTCURLYBRACKET", sym.RIGHTCURLYBRACKET);
            case "[" -> symbolFactory.newSymbol("LEFTSQUAREBRACKET", sym.LEFTSQUAREBRACKET);
            case "]" -> symbolFactory.newSymbol("RIGHTSQUAREBRACKET", sym.RIGHTSQUAREBRACKET);
            case "." -> symbolFactory.newSymbol("DOT", sym.DOT);
            case "," -> symbolFactory.newSymbol("COMMA", sym.COMMA);
            case ";" -> symbolFactory.newSymbol("SEMI", sym.SEMI);
            case "int" -> symbolFactory.newSymbol("INT", sym.INT);
            case "double" -> symbolFactory.newSymbol("DOUBLE", sym.DOUBLE);
            case "bool" -> symbolFactory.newSymbol("BOOL", sym.BOOL);
            case "string" -> symbolFactory.newSymbol("STRING", sym.STRING);
            case "define" -> symbolFactory.newSymbol("DEFINE", sym.DEFINE);
            case "import" -> symbolFactory.newSymbol("IMPORT", sym.IMPORT);
            case "void" -> symbolFactory.newSymbol("VOID", sym.VOID);
            case "class" -> symbolFactory.newSymbol("CLASS", sym.CLASS);
            case "public" -> symbolFactory.newSymbol("PUBLIC", sym.PUBLIC);
            case "private" -> symbolFactory.newSymbol("PRIVATE", sym.PRIVATE);
            case "if" -> symbolFactory.newSymbol("IF", sym.IF);
            case "else" -> symbolFactory.newSymbol("ELSE", sym.ELSE);
            case "for" -> symbolFactory.newSymbol("FOR", sym.FOR);
            case "while" -> symbolFactory.newSymbol("WHILE", sym.WHILE);
            case "return" -> symbolFactory.newSymbol("RETURN", sym.RETURN);
            case "break" -> symbolFactory.newSymbol("BREAK", sym.BREAK);
            case "continue" -> symbolFactory.newSymbol("CONTINUE", sym.CONTINUE);
            case "this" -> symbolFactory.newSymbol("THIS", sym.THIS);
            case "new" -> symbolFactory.newSymbol("NEW", sym.NEW);
            case "ReadInteger" -> symbolFactory.newSymbol("READINTEGER", sym.READINTEGER);
            case "ReadLine" -> symbolFactory.newSymbol("READLINE", sym.READLINE);
            case "NewArray" -> symbolFactory.newSymbol("NEWARRAY", sym.NEWARRAY);
            case "itod" -> symbolFactory.newSymbol("ITOD", sym.ITOD);
            case "itob" -> symbolFactory.newSymbol("ITOB", sym.ITOB);
            case "btoi" -> symbolFactory.newSymbol("BTOI", sym.BTOI);
            case "dtoi" -> symbolFactory.newSymbol("DTOI", sym.DTOI);
            case "__line__" -> symbolFactory.newSymbol("LINE", sym.LINE);
            case "__func__" -> symbolFactory.newSymbol("FUNC", sym.FUNC);
            case "null" -> symbolFactory.newSymbol("NULL", sym.NULL);
            case "T_STRINGLITERAL" -> symbolFactory.newSymbol("stringConstant", sym.stringConstant, splitTokens[1]);
            case "T_BOOLEANLITERAL" -> symbolFactory.newSymbol("boolConstant", sym.boolConstant, splitTokens[1]);
            case "T_ID" -> symbolFactory.newSymbol("ID", sym.ID, splitTokens[1]);
            case "T_INTLITERAL" -> symbolFactory.newSymbol("intConstant", sym.intConstant, splitTokens[1]);
            case "T_DOUBLELITERAL" -> symbolFactory.newSymbol("doubleConstant", sym.doubleConstant, splitTokens[1]);
            default -> symbolFactory.newSymbol("EOF", sym.EOF);
        };
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
        T_UNKNOWN
    }
