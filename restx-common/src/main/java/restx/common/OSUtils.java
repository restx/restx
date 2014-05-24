package restx.common;

/**
 * Date: 24/5/14
 * Time: 13:52
 */
public class OSUtils {
    public static boolean isMacOSX() {
        String osName = System.getProperty("os.name");
        return (osName.startsWith("Mac OS X") || osName.startsWith("Darwin"));
    }
}

