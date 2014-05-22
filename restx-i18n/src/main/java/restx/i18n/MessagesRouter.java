package restx.i18n;

import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import restx.*;
import restx.factory.Component;
import restx.http.CacheControl;
import restx.http.ETag;
import restx.http.HttpStatus;

import javax.inject.Named;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Map.Entry;

/**
 * Date: 2/2/14
 * Time: 08:41
 */
@Component
public class MessagesRouter extends RestxRouter {
    public MessagesRouter(@Named("Messages") Messages messages,
                          @Named("restx.i18n.labelsJsTemplate") String labelsJsTemplate) {
        super("MessagesRouter", new JsonLabelsRoute(messages), new JsLabelsRoute(messages, labelsJsTemplate));
    }

    private static class JsonLabelsRoute extends StdRoute {
        private final Messages messages;

        public JsonLabelsRoute(Messages messages) {
            super("labels.json", new StdRestxRequestMatcher("GET", "/i18n/labels.json"));
            this.messages = messages;
        }

        @Override
        public void handle(RestxRequestMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
            Iterable<Entry<String, String>> entries = messages.entries(req.getLocale());
            handleETagFor(req, resp, entries);
            resp.setStatus(HttpStatus.OK);
            resp.setContentType("application/json");

            PrintWriter writer = resp.getWriter();
            writeLabelsJson(writer, entries);
        }

    }

    private static class JsLabelsRoute extends StdRoute {
        private final Messages messages;
        private final String labelsJsBefore;
        private final String labelsJsAfter;

        public JsLabelsRoute(Messages messages, String labelsJsTemplate) {
            super("labels.js", new StdRestxRequestMatcher("GET", "/i18n/labels.js"));
            this.messages = messages;
            int i = labelsJsTemplate.indexOf("{LABELS}");
            if (i == -1) {
                throw new IllegalArgumentException("invalid labels js template. It must have {LABELS} token inside." +
                        "\n\tIt was :'" + labelsJsTemplate + "'");
            }
            labelsJsBefore = labelsJsTemplate.substring(0, i);
            labelsJsAfter = labelsJsTemplate.substring(i + "{LABELS}".length());
        }

        @Override
        public void handle(RestxRequestMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
            Iterable<Entry<String, String>> entries = messages.entries(req.getLocale());
            handleETagFor(req, resp, entries);

            resp.setStatus(HttpStatus.OK);
            resp.setContentType("application/javascript");

            PrintWriter writer = resp.getWriter();

            writer.print(labelsJsBefore);
            writeLabelsJson(writer, entries);
            writer.print(labelsJsAfter);
        }
    }

    protected static void handleETagFor(RestxRequest req, RestxResponse resp, Iterable<Entry<String, String>> entries) {
        Hasher hasher = Hashing.sha1().newHasher();
        for (Entry<String, String> entry : entries) {
            hasher.putString(entry.getKey(), Charsets.UTF_8).putString(entry.getValue(), Charsets.UTF_8);
        }
        new ETag(hasher.hash().toString(), CacheControl.MUST_REVALIDATE).handleIn(req, resp);
    }

    protected static void writeLabelsJson(PrintWriter writer, Iterable<Entry<String, String>> labels) {
        writer.println("{");

        boolean firstKey = true;
        for (Entry<String, String> entry : labels) {
            if (firstKey) {
                firstKey = false;
            } else {
                writer.print(",\n");
            }
            writer.print(String.format("  \"%s\" : \"%s\"", entry.getKey(), escape(entry.getValue())));
        }

        writer.print("\n}");
    }

    private static String escape(String str) {
        return str.replace("\"", "\\\"").replace("\n", "\\n");
    }
}
