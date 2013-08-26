package restx.server;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;

import java.io.IOException;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Iterables.getFirst;

/**
 * User: xavierhanin
 * Date: 2/20/13
 * Time: 1:51 PM
 */
public class HTTP {
    private static final Properties mimeTypes;
    private final static String RFC_2616_TOKEN_SPECIAL_CHARS_REGEX = "[\\s\\(\\)<>@,;:\\\\\"/\\[\\]\\?=\\{\\}]";

    static {
        mimeTypes = new Properties();
        try {
            mimeTypes.load(HTTP.class.getResourceAsStream("mime-types.properties"));
            for (String prop : mimeTypes.stringPropertyNames()) {
                Iterable<String> types = Splitter.on(",").omitEmptyStrings().trimResults().split(mimeTypes.getProperty(prop));
                mimeTypes.setProperty(prop, getFirst(types, "application/octet-stream"));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Optional<String> getContentTypeFromExtension(String filename) {
        String ext = filename.substring(filename.lastIndexOf('.') + 1);

        return Optional.fromNullable(mimeTypes.getProperty(ext));
    }

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

    public static String headerTokenCompatible(String s, String specialCharsReplacement) {
        checkArgument(
                specialCharsReplacement.replaceAll(RFC_2616_TOKEN_SPECIAL_CHARS_REGEX, "blah").equals(specialCharsReplacement),
                "specialCharsReplacement `%s` is not itself compatible with rfc 2616 !",
                specialCharsReplacement);

        // See rfc 2616 for allowed chars in header tokens (http://www.ietf.org/rfc/rfc2616.txt page 16)
        return s.replaceAll(RFC_2616_TOKEN_SPECIAL_CHARS_REGEX, specialCharsReplacement);
    }
}
