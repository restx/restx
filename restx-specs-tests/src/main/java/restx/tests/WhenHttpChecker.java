package restx.tests;

import com.chrylis.codec.base58.Base58Codec;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.base.Charsets;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import org.hamcrest.MatcherAssert;
import restx.factory.Component;
import restx.specs.WhenHttpRequest;
import restx.tests.json.JsonAssertions;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static restx.specs.WhenHttpRequest.BASE_URL;

@Component
public class WhenHttpChecker implements WhenChecker<WhenHttpRequest> {
    private List<String> headersSession = Arrays.asList("RestxSession", "RestxSessionSignature");

    @Override
    public Class<WhenHttpRequest> getWhenClass() {
        return WhenHttpRequest.class;
    }

    @Override
    public void check(WhenHttpRequest when, ImmutableMap<String, String> params) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        String baseUrl = checkNotNull(params.get(BASE_URL),
                BASE_URL + " param is required");
        String url = baseUrl + "/" + when.getPath();
        System.out.println("---------------------------------------------------------------------------------");
        System.out.println(">> REQUEST");
        System.out.println(when.getMethod() + " " + url);
        System.out.println();
        HttpTestClient httpTestClient = HttpTestClient.withBaseUrl(baseUrl);
        for (Map.Entry<String, String> cookie : when.getCookies().entrySet()) {
            httpTestClient = httpTestClient.withCookie(cookie.getKey(), cookie.getValue());
        }

        HttpRequest httpRequest = httpTestClient.http(when.getMethod(), when.getPath());

        ImmutableMap<String, String> cookies = when.getCookies();
        if (!cookies.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : cookies.entrySet()) {
                String valueCookie = headersSession.contains(entry.getKey())
                        ? Base58Codec.doEncode(entry.getValue().getBytes())
                        : entry.getValue();

                sb.append(entry.getKey()).append("=\"").append(valueCookie.replace("\"", "\\\"")).append("\"; ");
            }
            sb.setLength(sb.length() - 2);
            httpRequest.header("Cookie", sb.toString());
        }

        if (!Strings.isNullOrEmpty(when.getBody())) {
            httpRequest.contentType("application/json");
            httpRequest.send(when.getBody());
            System.out.println(when.getBody());
        }
        System.out.println();

        int code = httpRequest.code();
        System.out.println("<< RESPONSE");
        System.out.println(code);
        System.out.println();
        String body = httpRequest.body(Charsets.UTF_8.name());
        System.out.println(body);
        System.out.println();

        assertThat(code).isEqualTo(when.getThen().getExpectedCode());
        if (isJSON(when.getThen().getExpected())) {
            JsonAssertions.assertThat(body).allowingExtraUnexpectedFields().isSameJsonAs(when.getThen().getExpected());
        } else if (!when.getThen().getExpected().trim().isEmpty()) {
            MatcherAssert.assertThat(body.trim(), equalTo(when.getThen().getExpected().trim()));
        }
        System.out.printf("checked %s /%s -- %s%n", when.getMethod(), when.getPath(), stopwatch.stop().toString());
    }

    private boolean isJSON(String s) {
        // very basic impl of this, we could also parse it to check
        return s.trim().startsWith("{") || s.trim().startsWith("[");
    }
}
