package restx;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import restx.common.metrics.api.MetricRegistry;
import restx.common.metrics.api.Monitor;
import restx.common.metrics.dummy.DummyMetricRegistry;
import restx.exceptions.RestxError;
import restx.factory.Factory;
import restx.http.HttpStatus;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

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
        private MetricRegistry metrics;
        private String mode = RestxContext.Modes.PROD;
        private List<RestxFilter> filters = Lists.newArrayList();
        private List<RestxRoute> routes = Lists.newArrayList();

        public Builder withMetrics(MetricRegistry metrics) {
            this.metrics = metrics;
            return this;
        }

        public Builder inMode(String mode) {
            this.mode = mode;
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

        public RestxMainRouter build() {
            return new StdRestxMainRouter(
                    metrics == null ? getMetricsRegistryComponent() : metrics,
                    new RestxRouting(ImmutableList.copyOf(filters), ImmutableList.copyOf(routes)),
                    mode);
        }
    }

    private static MetricRegistry getMetricsRegistryComponent() {
        return Factory.getInstance().getComponent(MetricRegistry.class);
    }

    private static final Logger logger = LoggerFactory.getLogger(StdRestxMainRouter.class);

    private final RestxRouting routing;
    private final String mode;
    private final MetricRegistry metrics;

    public StdRestxMainRouter(RestxRouting routing) {
        this(routing, RestxContext.Modes.PROD);
    }

    public StdRestxMainRouter(RestxRouting routing, String mode) {
        this(getMetricsRegistryComponent(), routing, mode);
    }

    public StdRestxMainRouter(MetricRegistry metrics, RestxRouting routing, String mode) {
        this.metrics = checkNotNull(metrics);
        this.routing = checkNotNull(routing);
        this.mode = checkNotNull(mode);
    }

    @Override
    public void route(RestxRequest restxRequest, final RestxResponse restxResponse) throws IOException {
        logger.debug("<< {}", restxRequest);
        Stopwatch stopwatch = Stopwatch.createStarted();

        Monitor monitor = metrics.timer("<HTTP> " + restxRequest.getHttpMethod() + " " + restxRequest.getRestxPath()).time();
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
                restxResponse.setStatus(HttpStatus.NOT_FOUND);
                restxResponse.setContentType("text/plain");
                PrintWriter out = restxResponse.getWriter();
                out.print(sb.toString());
            } else {
                logger.debug("<< {}\nHANDLERS: {}", restxRequest, m.get().getMatches());
                RestxContext context = new RestxContext(getMode(), new AbstractRouteLifecycleListener() {},
                        ImmutableList.copyOf(m.get().getMatches()));
                RestxHandlerMatch match = context.nextHandlerMatch();
                match.handle(restxRequest, restxResponse, context);
            }
        } catch (JsonProcessingException ex) {
            logger.warn("request raised " + ex.getClass().getSimpleName(), ex);
            restxResponse.setStatus(HttpStatus.BAD_REQUEST);
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
                            if (location != null && (i + 1 == location.getLineNr())) {
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
        } catch (WebException ex) {
            ex.writeTo(restxRequest, restxResponse);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            logger.warn("request raised " + ex.getClass().getSimpleName() + ": " + ex.getMessage(), ex);
            restxResponse.setStatus(HttpStatus.BAD_REQUEST);
            restxResponse.setContentType("text/plain");
            PrintWriter out = restxResponse.getWriter();
            out.println("UNEXPECTED CLIENT ERROR:");
            out.print(ex.getMessage());
        } catch (Throwable ex) {
            logger.error("request raised " + ex.getClass().getSimpleName() + ": " + ex.getMessage(), ex);
            restxResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            restxResponse.setContentType("text/plain");
            PrintWriter out = restxResponse.getWriter();
            out.println("UNEXPECTED SERVER ERROR:");
            out.print(ex.getMessage());
        } finally {
            try { restxRequest.closeContentStream(); } catch (Exception ex) { }
            try { restxResponse.close(); } catch (Exception ex) { }
            monitor.stop();
            stopwatch.stop();
            restxResponse.getLogLevel().log(logger, restxRequest, restxResponse, stopwatch);
            MDC.clear();
        }
    }

    String getMode() {
        return mode;
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
