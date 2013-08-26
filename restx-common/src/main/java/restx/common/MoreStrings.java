package restx.common;

import com.google.common.base.Strings;

import java.util.regex.Pattern;

/**
 * User: xavierhanin
 * Date: 3/30/13
 * Time: 5:56 PM
 */
public class MoreStrings {
    public static String indent(String s, int i) {
        return Pattern.compile("^", Pattern.MULTILINE).matcher(s).replaceAll(Strings.repeat(" ", i));
    }

    public static String reindent(String s, int i) {
        return Pattern.compile("^\\s*", Pattern.MULTILINE).matcher(s).replaceAll(Strings.repeat(" ", i));
    }
}
