package restx.common;

import com.google.common.base.Function;
import com.google.common.base.Strings;

import java.util.regex.Pattern;

/**
 * User: xavierhanin
 * Date: 3/30/13
 * Time: 5:56 PM
 */
public class MoreStrings {
    public static final Function<String,String> SURROUND_WITH_DOUBLE_QUOTES = new Function<String, String>() {
        @Override
        public String apply(String input) {
            return input==null?null:String.format("\"%s\"", input);
        }
    };

    public static String indent(String s, int i) {
        return Pattern.compile("^", Pattern.MULTILINE).matcher(s).replaceAll(Strings.repeat(" ", i));
    }

    public static String reindent(String s, int i) {
        return Pattern.compile("^\\s*", Pattern.MULTILINE).matcher(s).replaceAll(Strings.repeat(" ", i));
    }

    public static String lowerFirstLetter(String str) {
        return str.substring(0, 1).toLowerCase()+str.substring(1);
    }
}
