package restx.specs;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.base.Charsets;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.fest.assertions.api.Assertions;
import org.hamcrest.MatcherAssert;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import uk.co.datumedge.hamcrest.json.SameJSONAs;

import java.util.List;
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

    public void run(ImmutableMap<String, String> params) {
        List<GivenCleaner> givenCleaners = Lists.newArrayList();
        for (Given given : getGiven()) {
            givenCleaners.add(given.run(params));
        }

        for (When when : getWhens()) {
            when.check(params);
        }

        for (GivenCleaner givenCleaner : givenCleaners) {
            givenCleaner.cleanUp();
        }
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

    public ImmutableList<Given> getGiven() {
        return given;
    }

    public ImmutableList<When> getWhens() {
        return whens;
    }

    public static interface Given {
        void toString(StringBuilder sb);
        public GivenCleaner run(ImmutableMap<String, String> params);
    }

    public static interface GivenCleaner {
        public void cleanUp();
    }

    public static class GivenTime implements Given {

        private final DateTime time;

        public GivenTime(DateTime time) {
            this.time = time;
        }

        @Override
        public GivenCleaner run(ImmutableMap<String, String> params) {
            DateTimeUtils.setCurrentMillisFixed(time.getMillis());

            return new GivenCleaner() {
                @Override
                public void cleanUp() {
                    DateTimeUtils.setCurrentMillisSystem();
                }
            };
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

        When(T then) {
            this.then = then;
        }

        public abstract void check(ImmutableMap<String, String> params);

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

        public void check(ImmutableMap<String, String> params) {
            Stopwatch stopwatch = new Stopwatch().start();
            String url = checkNotNull(params.get(BASE_URL),
                    BASE_URL + " param is required") + "/" + getPath();
            System.out.println("---------------------------------------------------------------------------------");
            System.out.println(">> REQUEST");
            System.out.println(getMethod() + " " + url);
            System.out.println();
            HttpRequest httpRequest = new HttpRequest(url, getMethod());

            if (!cookies.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, String> entry : cookies.entrySet()) {
                    sb.append(entry.getKey()).append("=").append(entry.getValue()).append("; ");
                }
                sb.setLength(sb.length() - 2);
                httpRequest.header("Cookie", sb.toString());
            }

            if (!Strings.isNullOrEmpty(getBody())) {
                httpRequest.contentType("application/json");
                httpRequest.send(getBody());
                System.out.println(getBody());
            }
            System.out.println();

            int code = httpRequest.code();
            System.out.println("<< RESPONSE");
            System.out.println(code);
            System.out.println();
            String body = httpRequest.body(Charsets.UTF_8.name());
            System.out.println(body);
            System.out.println();

            Assertions.assertThat(code).isEqualTo(getThen().getExpectedCode());
            MatcherAssert.assertThat(body,
                    SameJSONAs.sameJSONAs(getThen().getExpected()).allowingExtraUnexpectedFields());
            System.out.printf("checked %s /%s -- %s%n", getMethod(), getPath(), stopwatch.stop().toString());
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

    public static class Then {
        Then() {
        }
    }

    public static class ThenHttpResponse extends Then {
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
