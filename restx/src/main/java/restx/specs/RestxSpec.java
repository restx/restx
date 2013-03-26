package restx.specs;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.InputSupplier;
import com.google.common.io.Resources;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.fest.assertions.api.Assertions;
import org.hamcrest.MatcherAssert;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.yaml.snakeyaml.Yaml;
import uk.co.datumedge.hamcrest.json.SameJSONAs;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static restx.common.MorePreconditions.checkInstanceOf;

/**
* User: xavierhanin
* Date: 3/12/13
* Time: 9:51 PM
*/
public class RestxSpec {
    public static RestxSpec load(String resource) throws IOException {
        return load(Resources.newReaderSupplier(
                Resources.getResource(resource),
                Charset.forName("UTF-8")));
    }

    public static RestxSpec load(InputSupplier<InputStreamReader> inputSupplier) throws IOException {
        Yaml yaml = new Yaml();
        Map spec = (Map) yaml.load(inputSupplier.getInput());
        List<Given> givens = loadGivens(spec);
        List<When> whens = Lists.newArrayList();
        Iterable wts = checkInstanceOf("wts", spec.get("wts"), Iterable.class);
        for (Object wt : wts) {
            Map whenThen = checkInstanceOf("when/then", wt, Map.class);
            Object w = whenThen.get("when");
            if (w instanceof String) {
                String ws = (String) w;
                String definition;
                String body;
                Map<String, String> cookies = Maps.newLinkedHashMap();

                int nlIndex = ws.indexOf("\n");
                if (nlIndex != -1) {
                    definition = ws.substring(0, nlIndex);
                    body = ws.substring(nlIndex + 1).trim();

                    while (body.startsWith("Cookie:")) {
                        String cookieValues = body.substring("Cookie:".length());
                        for (String s : Splitter.on(";").trimResults().split(cookieValues)) {
                            int i = s.indexOf('=');

                            String name = s.substring(0, i);
                            String value = s.substring(i + 1);
                            cookies.put(name, value);
                        }

                        nlIndex = body.indexOf("\n");
                        body = nlIndex == -1 ? "" : body.substring(nlIndex + 1).trim();
                    }
                } else {
                    definition = ws;
                    body = "";
                }

                Matcher matcher = Pattern.compile("(GET|POST|PUT|DELETE|HEAD|OPTIONS) (.+)").matcher(definition);

                if (matcher.matches()) {
                    String method = matcher.group(1);
                    String path = matcher.group(2);
                    String then = checkInstanceOf("then", whenThen.get("then"), String.class).trim();
                    int code = 200;
                    int endLineIndex = then.indexOf("\n");
                    String firstLine = endLineIndex > 0 ? then.substring(0, endLineIndex) : "";
                    Matcher respMatcher = Pattern.compile("^(\\d{3}).*$").matcher(firstLine);
                    if (respMatcher.matches()) {
                        code = Integer.parseInt(respMatcher.group(1));
                        then = then.substring(endLineIndex).trim();
                    }
                    whens.add(new WhenHttpRequest(method, path, ImmutableMap.copyOf(cookies), body, new ThenHttpResponse(code, then)));
                } else {
                    throw new IllegalArgumentException("unrecognized 'when' format: it must begin with " +
                            "a HTTP declaration of the form 'VERB resource/path'\nEg: GET users/johndoe\n. Was: '" + ws + "'\n");
                }
            }
        }

        return new RestxSpec(checkInstanceOf("title", spec.get("title"), String.class),
                ImmutableList.copyOf(givens),
                ImmutableList.copyOf(whens));
    }

    private static List<Given> loadGivens(Map testCase) throws IOException {
        List<Given> givens = Lists.newArrayList();
        Iterable given = checkInstanceOf("given", testCase.get("given"), Iterable.class);
        for (Object g : given) {
            Map given1 = checkInstanceOf("given", g, Map.class);
            if (given1.containsKey("collection")) {
                String path = given1.containsKey("path") ? checkInstanceOf("path", given1.get("path"), String.class) : "data://";
                String data;
                if (given1.containsKey("data")) {
                    data = checkInstanceOf("data", given1.get("data"), String.class);
                } else if (given1.containsKey("path")) {
                    if (path.startsWith("/")) {
                        data = Resources.toString(Resources.getResource(path.substring(1)), Charset.forName("UTF-8"));
                    } else {
                        throw new IllegalArgumentException("only absolute resource paths are supported for collection data." +
                                " was: " + path + " in " + given1);
                    }
                } else {
                    data = "";
                }
                List<String> sequence = Lists.newArrayList();

                givens.add(new GivenCollection(
                        checkInstanceOf("collection", given1.get("collection"), String.class),
                        path,
                        data,
                        ImmutableList.copyOf(sequence)));
            } else {
                throw new IllegalArgumentException("invalid given " + given1 + ": unreconigzed type." +
                        " Was expecting one of ['collection'] field to be assigned");
            }
        }
        return givens;
    }

    private final String title;
    private final ImmutableList<Given> given;
    private final ImmutableList<When> whens;

    RestxSpec(String title, ImmutableList<Given> given, ImmutableList<When> whens) {
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
        public GivenCleaner run(ImmutableMap<String, String> params);
        void toString(StringBuilder sb);
    }

    public static interface GivenCleaner {
        public void cleanUp();
    }

    public static class GivenCollection implements Given {
        public static final String DB_URI = "GivenCollection.DB_URI";

        private final String collection;
        private final String path;
        private final String data;
        private final ImmutableList<String> sequence;

        GivenCollection(String collection, String path, String data, ImmutableList<String> sequence) {
            this.collection = collection;
            this.path = path;
            this.data = data;
            this.sequence = sequence;
        }

        @Override
        public void toString(StringBuilder sb) {
            sb.append("  - collection: ").append(collection).append("\n");
            if (!Strings.isNullOrEmpty(path) && !path.equals("data://")) {
                sb.append("    path: ").append(path).append("\n");
            }
            if (!data.isEmpty()) {
                sb.append("    data: |\n").append(reindent(data.trim(), 8)).append("\n");
            }
            if (!sequence.isEmpty()) {
                    sb.append("    sequence: ");
                    Joiner.on(", ").appendTo(sb, sequence);
                    sb.append("\n");
            }
        }

        public GivenCleaner run(final ImmutableMap<String, String> params) {
            try {
                MongoClientURI mongoClientURI = new MongoClientURI(
                        checkNotNull(params.get(DB_URI),
                                DB_URI + " param is required"));
                Jongo jongo = new Jongo(new MongoClient(mongoClientURI).getDB(mongoClientURI.getDatabase()));
                Stopwatch stopwatch = new Stopwatch().start();
                MongoCollection collection = jongo.getCollection(getCollection());
                Iterable<String> items = Splitter.on("\n").trimResults().omitEmptyStrings().split(getData());
                int count = 0;
                for (String item : items) {
                    collection.insert(item);
                    count++;
                }
                System.out.printf("imported %s[%d] -- %s%n", getCollection(), count, stopwatch.stop().toString());
                return new GivenCleaner() {
                    @Override
                    public void cleanUp() {
                        try {
                            MongoClientURI mongoClientURI = new MongoClientURI(
                                    checkNotNull(params.get(DB_URI),
                                            DB_URI + " param is required"));
                            Jongo jongo = new Jongo(new MongoClient(mongoClientURI).getDB(mongoClientURI.getDatabase()));
                            Stopwatch stopwatch = new Stopwatch().start();
                            jongo.getCollection(getCollection()).drop();
                            System.out.printf("dropped %s -- %s%n", getCollection(), stopwatch.stop().toString());
                        } catch (UnknownHostException e) {
                            throw new RuntimeException(e);
                        }
                    }
                };
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        }

        public String getCollection() {
            return collection;
        }

        public String getPath() {
            return path;
        }

        public String getData() {
            return data;
        }

        public ImmutableList<String> getSequence() {
            return sequence;
        }
    }

    public static class GivenTime implements Given {
        private final DateTime time;

        GivenTime(DateTime time) {
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
        public static final String BASE_URL = "WhenHttpRequest.BASE_URL";

        private final String method;
        private final String path;
        private final String body;
        private final ImmutableMap<String, String> cookies;

        WhenHttpRequest(String method, String path, ImmutableMap<String, String> cookies, String body, ThenHttpResponse then) {
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
                httpRequest.send(getBody());
                System.out.println(getBody());
            }
            System.out.println();

            int code = httpRequest.code();
            System.out.println("<< RESPONSE");
            System.out.println(code);
            System.out.println();
            String body = httpRequest.body("UTF-8");
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
                        .append("       ").append(method).append(" ").append(path).append("\n\n");
                if (!cookies.isEmpty()) {
                    sb.append("       Cookie: ");
                    for (Map.Entry<String, String> entry : cookies.entrySet()) {
                        sb.append(entry.getKey()).append("=").append(entry.getValue()).append("; ");
                    }
                    sb.setLength(sb.length() - 2);
                    sb.append("\n");
                }
                if (!Strings.isNullOrEmpty(body)) {
                    sb.append(indent(body.trim(), 8)).append("\n");
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

        ThenHttpResponse(int expectedCode, String expected) {
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

    private static String indent(String s, int i) {
        return Pattern.compile("^", Pattern.MULTILINE).matcher(s).replaceAll(Strings.repeat(" ", i));
    }

    private static String reindent(String s, int i) {
        return Pattern.compile("^\\s*", Pattern.MULTILINE).matcher(s).replaceAll(Strings.repeat(" ", i));
    }


}
