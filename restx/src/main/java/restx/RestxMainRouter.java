package restx;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.CaseFormat;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.io.CharStreams;
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
        if (getLoadFactoryMode().equals("onstartup")) {
            loadFactory("");
            String baseUri = System.getProperty("restx.baseUri", "");
            if (!baseUri.isEmpty()) {
                logger.info("\n" +
                        "--------------------------------------\n" +
                        " -- RESTX READY\n" +
                        " -- " + mainRouter.getNbRoutes() + " routes - " + factory.getNbMachines() + " factory machines\n" +
                        " -- for a list of available routes,\n" +
                        " --   VISIT " + baseUri + "/404\n" +
                        " --\n");
            } else {
                logger.info("RESTX READY");
            }
        }
    }

    private void loadFactory(String context) {
        factory = Factory.builder()
                .addFromServiceLoader()
                .addLocalMachines(Factory.LocalMachines.threadLocal())
                .addLocalMachines(Factory.LocalMachines.contextLocal(context))
                .build();

        logger.debug("restx factory ready: {}", factory);

        mainRouter = new RestxRouter("MainRouter", ImmutableList.copyOf(factory.getComponents(RestxRoute.class)));
    }


    public void route(String contextName, RestxRequest restxRequest, final RestxResponse restxResponse) throws IOException {
        logger.info("<< {}", restxRequest);
        if (getLoadFactoryMode().equals("onrequest")) {
            loadFactory(contextName);
        }

        try {
            if (!mainRouter.route(restxRequest, restxResponse,
                    new RestxContext(RouteLifecycleListener.DEAF))) {
                String path = restxRequest.getRestxPath();
                String msg = String.format(
                        "no restx route found for %s %s\n" +
                        "routes:\n" +
                        "-----------------------------------\n" +
                        "%s\n" +
                        "-----------------------------------",
                        restxRequest.getHttpMethod(), path, mainRouter);
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
                            out.print(Strings.repeat(" ", location.getColumnNr() - 2));
                            out.println("^");
                            out.print(">> ");
                            out.print(Strings.repeat(" ", Math.max(0, location.getColumnNr()
                                    - (ex.getOriginalMessage().length() / 2) - 3)));
                            out.print(ex.getOriginalMessage());
                            out.println(" <<");
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
        }
    }

    private String getLoadFactoryMode() {
        return System.getProperty("restx.factory.load", "onstartup");
    }


}
