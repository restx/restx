package restx.specs;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

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
