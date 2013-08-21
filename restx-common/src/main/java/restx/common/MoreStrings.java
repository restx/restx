package restx.common;

import com.google.common.base.Strings;

import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * User: xavierhanin
 * Date: 3/30/13
 * Time: 5:56 PM
 */
public class MoreStrings {
    private final static String RFC_2616_TOKEN_SPECIAL_CHARS_REGEX = "[\\s\\(\\)<>@,;:\\\\\"/\\[\\]\\?=\\{\\}]";

    public static String indent(String s, int i) {
        return Pattern.compile("^", Pattern.MULTILINE).matcher(s).replaceAll(Strings.repeat(" ", i));
    }

    public static String reindent(String s, int i) {
        return Pattern.compile("^\\s*", Pattern.MULTILINE).matcher(s).replaceAll(Strings.repeat(" ", i));
    }

    public static String headerTokenCompatible(String s, String specialCharsReplacement) {
        checkArgument(
                specialCharsReplacement.replaceAll(RFC_2616_TOKEN_SPECIAL_CHARS_REGEX, "blah").equals(specialCharsReplacement),
                "specialCharsReplacement `%s` is not itself compatible with rfc 2616 !",
                specialCharsReplacement);

        // See rfc 2616 for allowed chars in header tokens (http://www.ietf.org/rfc/rfc2616.txt page 16)
        return s.replaceAll(RFC_2616_TOKEN_SPECIAL_CHARS_REGEX, specialCharsReplacement);
    }
}
