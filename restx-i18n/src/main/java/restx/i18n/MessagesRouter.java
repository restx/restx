package restx.i18n;

import restx.*;
import restx.factory.Component;
import restx.http.HttpStatus;

import javax.inject.Named;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

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
            resp.setStatus(HttpStatus.OK);
            resp.setContentType("application/json");
            Locale locale = req.getLocale();

            PrintWriter writer = resp.getWriter();
            writeLabelsJson(locale, writer, messages);
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
            resp.setStatus(HttpStatus.OK);
            resp.setContentType("application/javascript");
            Locale locale = req.getLocale();

            PrintWriter writer = resp.getWriter();

            writer.print(labelsJsBefore);
            writeLabelsJson(locale, writer, messages);
            writer.print(labelsJsAfter);
        }
    }

    protected static void writeLabelsJson(Locale locale, PrintWriter writer, Messages msgs) {
        writer.println("{");

        boolean firstKey = true;
        for (String key : msgs.keys()) {
            if (firstKey) {
                firstKey = false;
            } else {
                writer.print(",\n");
            }
            writer.print("  \"" + key + "\" : \"" + escape(msgs.getMessageTemplate(key, locale)) + "\"");
        }

        writer.print("\n}");
    }

    private static String escape(String str) {
        return str.replace("\"", "\\\"").replace("\n", "\\n");
    }
}
