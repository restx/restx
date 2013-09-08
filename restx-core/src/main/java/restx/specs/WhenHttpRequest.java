package restx.specs;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static com.google.common.collect.Maps.newLinkedHashMap;
import static restx.common.MoreStrings.indent;

/**
* @author xavierhanin
*/
public class WhenHttpRequest extends When<ThenHttpResponse> {
    public static final String CONTEXT_NAME = "WhenHttpRequest.CONTEXT_NAME";
    public static final String BASE_URL = "WhenHttpRequest.BASE_URL";

    private final String method;
    private final String path;
    private final String body;
    private final ImmutableMap<String, String> cookies;

    public WhenHttpRequest(String method, String path, Map<String, String> cookies, String body, ThenHttpResponse then) {
        super(then);
        this.method = method;
        this.path = path;
        this.body = body;
        this.cookies = ImmutableMap.copyOf(cookies);
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

    @Override
    public When<ThenHttpResponse> withThen(ThenHttpResponse then) {
        return new WhenHttpRequest(method, path, cookies, body, then);
    }

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder {
        private String method;
        private String path;
        private String body;
        private Map<String, String> cookies = newLinkedHashMap();
        private ThenHttpResponse response;

        public Builder withMethod(String method) {
            this.method = method;
            return this;
        }

        public Builder withPath(String path) {
            this.path = path;
            return this;
        }

        public Builder withBody(String body) {
            this.body = body;
            return this;
        }

        public Builder addCookie(String key, String value) {
            this.cookies.put(key, value);
            return this;
        }

        public boolean containsCookie(String key) {
            return this.cookies.containsKey(key);
        }

        public Builder withThen(ThenHttpResponse response) {
            this.response = response;
            return this;
        }

        public WhenHttpRequest build() {
            return new WhenHttpRequest(method, path, cookies, body, response);
        }
    }
}
