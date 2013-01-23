package restx.jongo;

import java.util.regex.Pattern;

/**
 * User: xavierhanin
 * Date: 1/23/13
 * Time: 6:42 PM
 */
public class Jongos {
    public static Pattern startingWith(String expr) {
        return Pattern.compile(String.format("\\Q%s\\E.*", expr));
    }
}
