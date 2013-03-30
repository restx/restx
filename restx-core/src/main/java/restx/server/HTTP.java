package restx.server;

import com.google.common.base.Optional;

/**
 * User: xavierhanin
 * Date: 2/20/13
 * Time: 1:51 PM
 */
public class HTTP {
    public static boolean isTextContentType(String contentType) {
        // the list is not fully exhaustive, should cover most cases.
        return contentType.startsWith("text/")
                || contentType.startsWith("application/json")
                || contentType.startsWith("application/javascript")
                || contentType.startsWith("application/ecmascript")
                || contentType.startsWith("application/atom+xml")
                || contentType.startsWith("application/rss+xml")
                || contentType.startsWith("application/xhtml+xml")
                || contentType.startsWith("application/soap+xml")
                || contentType.startsWith("application/xml")
                ;
    }

    public static Optional<String> charsetFromContentType(String s) {
        if (s.indexOf("charset=") == -1) {
            return Optional.absent();
        } else {
            return Optional.of(s.substring(s.indexOf("charset=") + "charset=".length()));
        }
    }
}
