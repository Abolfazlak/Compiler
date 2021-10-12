import java.util.regex.Pattern;
public class Scanner {}
    class Patterns {
        Pattern _while = Pattern.compile("(while)(\\s*)(\\()(.*)(\\))");
        Pattern _if = Pattern.compile("(if)(\\s*)(\\()(.*)(\\))");
        Pattern _for = Pattern.compile("(for)(\\s*)(\\()(.*)(\\))");
        Pattern _else = Pattern.compile("(else)");
        Pattern _id = Pattern.compile("([a-z]|[A-Z]).([a-z]|[A-Z]|[0-9]|\\_)*");
        Pattern _int10 = Pattern.compile("([0-9])*");
        Pattern _int16 = Pattern.compile("0(x|X).([A-F]|[a-f]|[0-9])*");
        Pattern _double = Pattern.compile("([0-9])+(\\.)([0-9])*");
        Pattern _SN = Pattern.compile("([0-9]+)(\\.)([0-9]*)(e|E)(\\+|\\-?)([0-9])");
        Pattern _operators = Pattern.compile("(\\+(\\=?))|(\\-(\\=?))|(\\*(\\=?))|(\\/(\\=?))|(\\%)|(\\<(\\=?))|(\\>(\\=?))|(\\=(\\=?))|(\\!(\\=?))|(\\&\\&)|(\\|\\|)|(\\;)|(\\,)|(\\.)|(\\[)|(\\])|(\\()|(\\))|(\\{)|(\\})");
        Pattern _comment = Pattern.compile("((\\/\\/)(.*))|((\\/\\*)((.*)(\\n?))*(\\*\\/))");

    }
