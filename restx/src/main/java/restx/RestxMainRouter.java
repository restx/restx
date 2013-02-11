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
import restx.factory.Factory;

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
public class RestxMainRouter {

    private final Logger logger = LoggerFactory.getLogger(RestxMainRouter.class);

    private Factory factory;
    private RestxRouter mainRouter;

    public void init() {
        String baseUri = System.getProperty("restx.baseUri", "");
        if (getLoadFactoryMode().equals("onstartup")) {
            loadFactory("");
            if (!baseUri.isEmpty()) {
                logPrompt(baseUri, "READY");
            } else {
                logger.info("RESTX READY");
            }
        } else {
            if (!baseUri.isEmpty()) {
                logPrompt(baseUri, "LOAD ON REQUEST");
            }
        }
    }

    private void logPrompt(String baseUri, String state) {
        logger.info("\n" +
                "--------------------------------------\n" +
                " -- RESTX " + state + "\n" +
                (mainRouter != null ? (" -- " + mainRouter.getNbRoutes() + " routes - " + factory.getNbMachines() + " factory machines\n") : "") +
                " -- for api documentation,\n" +
                " --   VISIT " + baseUri + "/@/api-docs-ui\n" +
                " --\n");
    }

    private void loadFactory(String context) {
        factory = Factory.builder()
                .addFromServiceLoader()
                .addLocalMachines(Factory.LocalMachines.threadLocal())
                .addLocalMachines(Factory.LocalMachines.contextLocal(context))
                .build();

        logger.debug("restx factory ready: {}", factory);

        mainRouter = new RestxRouter("MainRouter", ImmutableList.copyOf(
                factory.queryByClass(RestxRoute.class).findAsComponents()));
    }


    public void route(String contextName, RestxRequest restxRequest, final RestxResponse restxResponse) throws IOException {
        logger.info("<< {}", restxRequest);
        if (getLoadFactoryMode().equals("onrequest")) {
            loadFactory(contextName);
        }

        Monitor monitor = MonitorFactory.start("<HTTP> " + restxRequest.getHttpMethod() + " " + restxRequest.getRestxPath());
        try {
            if (!mainRouter.route(restxRequest, restxResponse,
                    new RestxContext(RouteLifecycleListener.DEAF))) {
                String path = restxRequest.getRestxPath();
                String msg = String.format(
                        "no restx route found for %s %s\n" +
                        "go to %s for API documentation\n\n" +
                        "routes:\n" +
                        "-----------------------------------\n" +
                        "%s\n" +
                        "-----------------------------------",
                        restxRequest.getHttpMethod(), path,
                        restxRequest.getBaseUri() + "/@/api-docs-ui",
                        mainRouter);
                restxResponse.setStatus(404);
                restxResponse.setContentType("text/plain");
                PrintWriter out = restxResponse.getWriter();
                out.print(msg);
                out.close();
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
            out.close();
        } catch (IllegalArgumentException ex) {
            logger.debug("request raised IllegalArgumentException", ex);
            restxResponse.setStatus(400);
            restxResponse.setContentType("text/plain");
            PrintWriter out = restxResponse.getWriter();
            out.print(ex.getMessage());
            out.close();
        } finally {
            try { restxRequest.closeContentStream(); } catch (Exception ex) { }
            monitor.stop();
        }
    }

    private String getLoadFactoryMode() {
        return System.getProperty("restx.factory.load", "onstartup");
    }


}
