package compiler;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Scanner {

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
