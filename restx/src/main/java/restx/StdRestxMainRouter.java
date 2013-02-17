package restx;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.CaseFormat;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.io.CharStreams;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;

/**
 * User: xavierhanin
 * Date: 2/6/13
 * Time: 9:53 PM
 */
public class StdRestxMainRouter implements RestxMainRouter {

    private final Logger logger = LoggerFactory.getLogger(StdRestxMainRouter.class);

    private RestxRouter mainRouter;

    public StdRestxMainRouter(ImmutableList<RestxRoute> routes) {
        mainRouter = new RestxRouter("MainRouter", routes);
    }

    @Override
    public void route(RestxRequest restxRequest, final RestxResponse restxResponse) throws IOException {
        logger.info("<< {}", restxRequest);

        Monitor monitor = MonitorFactory.start("<HTTP> " + restxRequest.getHttpMethod() + " " + restxRequest.getRestxPath());
        try {
            if (!mainRouter.route(restxRequest, restxResponse,
                    new RestxContext(RouteLifecycleListener.DEAF))) {
                String path = restxRequest.getRestxPath();
                StringBuilder sb = new StringBuilder()
                        .append("no restx route found for ")
                        .append(restxRequest.getHttpMethod()).append(" ").append(path).append("\n");
                if (hasApiDocs()) {
                    sb.append("go to ").append(restxRequest.getBaseUri()).append("/@/api-docs-ui")
                            .append(" for API documentation\n\n");
                }
                sb.append("routes:\n")
                        .append("-----------------------------------\n")
                        .append(mainRouter).append("\n")
                        .append("-----------------------------------");
                restxResponse.setStatus(404);
                restxResponse.setContentType("text/plain");
                PrintWriter out = restxResponse.getWriter();
                out.print(sb.toString());
            }
        } catch (JsonProcessingException ex) {
            logger.debug("request raised " + ex.getClass().getSimpleName(), ex);
            restxResponse.setStatus(400);
            restxResponse.setContentType("text/plain");
            PrintWriter out = restxResponse.getWriter();
            if (restxRequest.getContentStream() instanceof BufferedInputStream) {
                try {
                    JsonLocation location = ex.getLocation();
                    restxRequest.getContentStream().reset();
                    out.println(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, ex.getClass().getSimpleName()) + "." +
                            " Please verify your input:");
                    out.println("<-- JSON -->");
                    List<String> lines = CharStreams.readLines(
                            new InputStreamReader(restxRequest.getContentStream()));
                    for (int i = 0; i < lines.size(); i++) {
                        String line = lines.get(i);
                        out.println(line);
                        if (i + 1 == location.getLineNr()) {
                            boolean farColumn = location.getColumnNr() > 80;
                            /*
                             * if error column is too far, we precede the error message with >> to show
                             * that there is an error message on the line
                             */
                            out.println(
                                    Strings.repeat(" ", Math.max(0, location.getColumnNr() - 2)) + "^");
                            out.println(Strings.repeat(farColumn ? ">" : " ", Math.max(0, location.getColumnNr()
                                    - (ex.getOriginalMessage().length() / 2) - 3))
                                    + ">> " + ex.getOriginalMessage() + " <<");
                            out.println();
                        }
                    }
                    out.println("</- JSON -->");

                    restxRequest.getContentStream().reset();
                    logger.debug(ex.getClass().getSimpleName() + " on " + restxRequest + "." +
                            " message: " + ex.getMessage() + "." +
                            " request content: " + CharStreams.toString(new InputStreamReader(restxRequest.getContentStream())));
                } catch (IOException e) {
                    logger.warn("io exception raised when trying to provide original input to caller", e);
                    out.println(ex.getMessage());
                }
            }
        } catch (IllegalArgumentException ex) {
            logger.debug("request raised IllegalArgumentException", ex);
            restxResponse.setStatus(400);
            restxResponse.setContentType("text/plain");
            PrintWriter out = restxResponse.getWriter();
            out.print(ex.getMessage());
        } finally {
            try { restxRequest.closeContentStream(); } catch (Exception ex) { }
            try { restxResponse.close(); } catch (Exception ex) { }
            monitor.stop();
        }
    }

    private boolean hasApiDocs() {
        for (RestxRoute route : mainRouter.getRoutes()) {
            // maybe we should find a more pluggable way to detect this feature..
            // we don't use the class itself, we don't want to have a strong dependency on swagger route
            if (route.getClass().getName().endsWith("SwaggerUIRoute")) {
                return true;
            }
        }

        return false;
    }

    public int getNbRoutes() {
        return mainRouter.getNbRoutes();
    }
}
