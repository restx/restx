package restx.server;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;

import java.io.IOException;
import java.util.Properties;

import static com.google.common.collect.Iterables.getFirst;

/**
 * User: xavierhanin
 * Date: 2/20/13
 * Time: 1:51 PM
 */
public class HTTP {
    private static final Properties mimeTypes;

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
}
