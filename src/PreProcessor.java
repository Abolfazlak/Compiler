import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PreProcessor {
    public Map<String, String> mapDefine (String input) {
        Map<String, String> defineMap = new HashMap<>();

        Pattern define = Pattern.compile("^[ \\t]*define[ \\t]+(\\S+)[ \\t]*((?:.*\\\\\\r?\\n)*.*)");
        Matcher matcher = define.matcher(input);

        while (matcher.find()) {
            defineMap.put(matcher.group(1), matcher.group(2));
        }
        return defineMap;
    }
}
