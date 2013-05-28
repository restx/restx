package restx.specs;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static restx.common.MoreStrings.indent;

/**
* User: xavierhanin
* Date: 3/12/13
* Time: 9:51 PM
*/
public class RestxSpec {
    private final String title;
    private final ImmutableList<Given> given;
    private final ImmutableList<When> whens;

    public RestxSpec(String title, ImmutableList<Given> given, ImmutableList<When> whens) {
        checkNotNull(title);
        this.title = title;
        this.given = given;
        this.whens = whens;
    }

    /**
     * Stores this recorded spec as a .spec.yaml file.
     *
     * @param path the path where this spec should be stored, relative to restx.recorder.basePath system property
     * @param title the spec title, use the recorded one if absent
     * @return the file where the spec has been stored
     *
     * @throws IOException in case of IO error while saving file.
     */
    public File store(Optional<String> path, Optional<String> title) throws IOException {
        File destFile = getStoreFile(path, title);
        store(destFile, title);
        return destFile;
    }

    public void store(File destFile, Optional<String> title) throws IOException {
        destFile.getParentFile().mkdirs();

        Files.write(withTitle(title.or(getTitle())).toString(),
                destFile, Charsets.UTF_8);
    }

    public File getStoreFile(Optional<String> path, Optional<String> title) {
        String basePath = System.getProperty("restx.recorder.basePath", "src/main/resources/specs");
        return new File(basePath + "/" + path.or("") + "/"
                + title.or(getTitle()).replace(' ', '_').replace('/', '_') + ".spec.yaml");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("title: ").append(title).append("\n");
        if (!given.isEmpty()) {
            sb.append("given:\n");
            for (RestxSpec.Given g : given) {
                g.toString(sb);
            }
        }
        sb.append("wts:\n");
        for (When when : whens) {
            when.toString(sb);
        }
        return sb.toString();
    }

    public String getTitle() {
        return title;
    }

    public RestxSpec withTitle(String title) {
        return new RestxSpec(title, given, whens);
    }

    public ImmutableList<Given> getGiven() {
        return given;
    }

    public ImmutableList<When> getWhens() {
        return whens;
    }

    public static interface Given {
        void toString(StringBuilder sb);
    }

    public static class GivenTime implements Given {

        private final DateTime time;

        public GivenTime(DateTime time) {
            this.time = time;
        }

        public DateTime getTime() {
            return time;
        }

        @Override
        public void toString(StringBuilder sb) {
            sb.append("  - time: ").append(time.toString()).append("\n");
        }
    }

    public static abstract class When<T extends Then> {
        private final T then;

        protected When(T then) {
            this.then = then;
        }

        public T getThen() {
            return then;
        }

        public abstract void toString(StringBuilder sb);
    }

    public static class WhenHttpRequest extends When<ThenHttpResponse> {
        public static final String CONTEXT_NAME = "WhenHttpRequest.CONTEXT_NAME";
        public static final String BASE_URL = "WhenHttpRequest.BASE_URL";

        private final String method;
        private final String path;
        private final String body;
        private final ImmutableMap<String, String> cookies;

        public WhenHttpRequest(String method, String path, ImmutableMap<String, String> cookies, String body, ThenHttpResponse then) {
            super(then);
            this.method = method;
            this.path = path;
            this.body = body;
            this.cookies = cookies;
        }

        @Override
        public void toString(StringBuilder sb) {
            if (Strings.isNullOrEmpty(body) && cookies.isEmpty()) {
                sb.append("  - when: ").append(method).append(" ").append(path).append("\n");
            } else {
                sb.append("  - when: |\n")
                        .append("       ").append(method).append(" ").append(path).append("\n");
                if (!cookies.isEmpty()) {
                    sb.append("       Cookie: ");
                    for (Map.Entry<String, String> entry : cookies.entrySet()) {
                        sb.append(entry.getKey()).append("=").append(entry.getValue()).append("; ");
                    }
                    sb.setLength(sb.length() - 2);
                    sb.append("\n");
                }
                if (!Strings.isNullOrEmpty(body)) {
                    sb.append("\n").append(indent(body.trim(), 8)).append("\n");
                }
            }
            getThen().toString(sb);
        }

        public String getMethod() {
            return method;
        }

        public String getPath() {
            return path;
        }

        public String getBody() {
            return body;
        }

        public ImmutableMap<String, String> getCookies() {
            return cookies;
        }
    }

    public static interface Then {
    }

    public static class ThenHttpResponse implements Then {
        private final int expectedCode;
        private final String expected;

        public ThenHttpResponse(int expectedCode, String expected) {
            this.expectedCode = expectedCode;
            this.expected = expected;
        }

        public String getExpected() {
            return expected;
        }

        public int getExpectedCode() {
            return expectedCode;
        }

        public void toString(StringBuilder sb) {
            sb.append("    then: |\n");
            if (expectedCode != 200) {
                sb.append("       ").append(expectedCode).append("\n\n");
            }
            sb.append(indent(expected, 8)).append("\n");
        }
    }

}
