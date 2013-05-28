package restx;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.CaseFormat;
import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.exceptions.RestxError;

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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ObjectMapper mapper;
        private List<RestxFilter> filters = Lists.newArrayList();
        private List<RestxRoute> routes = Lists.newArrayList();

        public Builder withMapper(ObjectMapper mapper) {
            this.mapper = mapper;
            return this;
        }

        public Builder addFilter(RestxFilter filter) {
            filters.add(filter);
            return this;
        }

        public Builder addRouter(RestxRouter router) {
            routes.addAll(router.getRoutes());
            return this;
        }

        public Builder addRoute(RestxRoute route) {
            routes.add(route);
            return this;
        }

        public Builder addRoute(String method, String path, final MatchedEntityRoute route) {
            routes.add(new StdEntityRoute(path, mapper, new StdRouteMatcher(method, path)) {
                @Override
                protected Optional<?> doRoute(RestxRequest restxRequest, RestxRouteMatch match) throws IOException {
                    return route.route(restxRequest, match);
                }
            });
            return this;
        }

        public RestxMainRouter build() {
            return new StdRestxMainRouter(new RestxRouting(
                    ImmutableList.copyOf(filters), ImmutableList.copyOf(routes)));
        }
    }

    private final Logger logger = LoggerFactory.getLogger(StdRestxMainRouter.class);

    private final RestxRouting routing;

    public StdRestxMainRouter(RestxRouting routing) {
        this.routing = routing;
    }

    @Override
    public void route(RestxRequest restxRequest, final RestxResponse restxResponse) throws IOException {
        logger.debug("<< {}", restxRequest);
        Stopwatch stopwatch = new Stopwatch().start();

        Monitor monitor = MonitorFactory.start("<HTTP> " + restxRequest.getHttpMethod() + " " + restxRequest.getRestxPath());
        try {
            Optional<RestxRouting.Match> m = routing.match(restxRequest);

            if (!m.isPresent()) {
                // no route matched
                String path = restxRequest.getRestxPath();
                StringBuilder sb = new StringBuilder()
                        .append("no restx route found for ")
                        .append(restxRequest.getHttpMethod()).append(" ").append(path).append("\n");
                if (hasApiDocs()) {
                    sb.append("go to ").append(restxRequest.getBaseUri()).append("/@/ui/api-docs/")
                            .append(" for API documentation\n\n");
                }
                sb.append("routes:\n")
                        .append("-----------------------------------\n");
                for (RestxRoute route : routing.getRoutes()) {
                    sb.append(route).append("\n");
                }
                sb.append("-----------------------------------");
                restxResponse.setStatus(404);
                restxResponse.setContentType("text/plain");
                PrintWriter out = restxResponse.getWriter();
                out.print(sb.toString());
            } else {
                RouteLifecycleListener noCache = new RouteLifecycleListener() {
                    @Override
                    public void onRouteMatch(RestxRoute source) {
                    }

                    @Override
                    public void onBeforeWriteContent(RestxRoute source) {
                        restxResponse.setHeader("Cache-Control", "no-cache");
                    }
                };
                RestxContext context = new RestxContext(getMode(restxRequest), noCache, ImmutableList.copyOf(m.get().getMatches()));
                RestxRouteMatch match = context.nextHandlerMatch();
                match.getHandler().handle(match, restxRequest, restxResponse, context);
            }
        } catch (JsonProcessingException ex) {
            logger.warn("request raised " + ex.getClass().getSimpleName(), ex);
            restxResponse.setStatus(400);
            restxResponse.setContentType("text/plain");
            PrintWriter out = restxResponse.getWriter();
            if (restxRequest.getContentStream() instanceof BufferedInputStream) {
                try {
                    JsonLocation location = ex.getLocation();
                    restxRequest.getContentStream().reset();
                    out.println(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, ex.getClass().getSimpleName()) + "." +
                            " Please verify your input:");
                    List<String> lines = CharStreams.readLines(
                            new InputStreamReader(restxRequest.getContentStream()));

                    if (lines.isEmpty()) {
                        if ("application/x-www-form-urlencoded".equalsIgnoreCase(restxRequest.getContentType())) {
                            out.println("Body was considered as parameter due to Content-Type: " +
                                    restxRequest.getContentType() + ". " +
                                    "Setting your Content-Type to \"application/json\" may resolve the problem");
                        } else {
                            out.println("Empty body. Content-type was \"" + restxRequest.getContentType() + "\"");
                        }
                    } else {
                        out.println("<-- JSON -->");
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
                    }

                    restxRequest.getContentStream().reset();
                    logger.debug(ex.getClass().getSimpleName() + " on " + restxRequest + "." +
                            " message: " + ex.getMessage() + "." +
                            " request content: " + CharStreams.toString(new InputStreamReader(restxRequest.getContentStream())));
                } catch (IOException e) {
                    logger.warn("io exception raised when trying to provide original input to caller", e);
                    out.println(ex.getMessage());
                }
            }
        } catch (RestxError.RestxException ex) {
            logger.debug("request raised RestxException", ex);
            restxResponse.setStatus(ex.getErrorStatus());
            restxResponse.setContentType("application/json");
            PrintWriter out = restxResponse.getWriter();
            out.println(ex.toJSON());
        } catch (IllegalArgumentException | IllegalStateException ex) {
            logger.warn("request raised " + ex.getClass().getSimpleName() + ": " + ex.getMessage(), ex);
            restxResponse.setStatus(400);
            restxResponse.setContentType("text/plain");
            PrintWriter out = restxResponse.getWriter();
            out.println("UNEXPECTED CLIENT ERROR:");
            out.print(ex.getMessage());
        } catch (RuntimeException ex) {
            logger.error("request raised " + ex.getClass().getSimpleName() + ": " + ex.getMessage(), ex);
            restxResponse.setStatus(500);
            restxResponse.setContentType("text/plain");
            PrintWriter out = restxResponse.getWriter();
            out.println("UNEXPECTED SERVER ERROR:");
            out.print(ex.getMessage());
        } finally {
            try { restxRequest.closeContentStream(); } catch (Exception ex) { }
            try { restxResponse.close(); } catch (Exception ex) { }
            monitor.stop();
            stopwatch.stop();
            logger.info("<< {} >> {} - {}", restxRequest, restxResponse.getStatus(), stopwatch);
        }
    }

    static String getMode() {
        return System.getProperty("restx.mode", RestxContext.Modes.PROD);
    }

    static String getMode(RestxRequest req) {
        return req.getHeader("RestxMode").or(getMode());
    }

    private boolean hasApiDocs() {
        for (RestxRoute route : routing.getRoutes()) {
            // maybe we should find a more pluggable way to detect this feature..
            // we don't use the class itself, we don't want to have a strong dependency on swagger route
            if (route.getClass().getName().endsWith("ApiDocsUIRoute")) {
                return true;
            }
        }

        return false;
    }

    public int getNbFilters() {
        return routing.getFilters().size();
    }

    public int getNbRoutes() {
        return routing.getRoutes().size();
    }
}
