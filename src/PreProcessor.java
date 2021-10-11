import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PreProcessor {
    public String removeComments(String input) {
        Pattern comment = Pattern.compile("(/\\*(?:.|[\\n\\r])*?\\*/)|(//.*)");
        Matcher matcher = comment.matcher(input);
        return matcher.replaceAll("");
    }

    public Matcher findDefine(String input){
        Pattern define = Pattern.compile("[ \\t]*define[ \\t]+(\\S+)[ \\t]*((?:.*\\\\\\r?\\n)*.*)");
        return define.matcher(input);
    }

    public Map<String, String> mapDefine (String input) {
        Map<String, String> defineMap = new HashMap<>();
        Matcher matcher = findDefine(input);

        while (matcher.find()) {
            defineMap.put(matcher.group(1), matcher.group(2));
        }
        return defineMap;
    }
    public String removeDefine (String input){
        Matcher matcher = findDefine(input);
        return matcher.replaceAll("");
    }

    public String replaceDefine (String input){
        AtomicReference<String> out = new AtomicReference<>(removeDefine(input));
        Map<String, String> define = mapDefine(input);
        define.forEach((key,value) -> {
            Pattern pattern = Pattern.compile("\\b"+key+"\\b");
            Matcher matcher = pattern.matcher(out.toString());
            while (matcher.find()){
                out.set(matcher.replaceAll(value));
            }
        });
        return out.toString();
    }
}
