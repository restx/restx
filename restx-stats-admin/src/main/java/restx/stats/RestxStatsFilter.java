package restx.stats;

import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import restx.RestxContext;
import restx.RestxFilter;
import restx.RestxHandler;
import restx.RestxHandlerMatch;
import restx.RestxRequest;
import restx.RestxRequestMatch;
import restx.RestxResponse;
import restx.StdRestxRequestMatch;
import restx.factory.Component;

import java.io.IOException;

/**
 * A filter used to notifiy the stats collector of all requests performed on the server.
 */
@Component(priority = -1000)
public final class RestxStatsFilter implements RestxFilter, RestxHandler {
    /**
     * We create a single match to minimize impact on performance,
     * we don't need to reuse any information on the match itself anyway.
     */
    private final Optional<RestxHandlerMatch> match = Optional.of(new RestxHandlerMatch(new StdRestxRequestMatch("/*"), this));

    private final RestxStatsCollector collector;

    public RestxStatsFilter(RestxStatsCollector collector) {
        this.collector = collector;
    }

    @Override
    public Optional<RestxHandlerMatch> match(RestxRequest req) {
        return match;
    }

    @Override
    public void handle(RestxRequestMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            ctx.nextHandlerMatch().handle(req, resp, ctx);
        } finally {
            collector.notifyRequest(req, resp, stopwatch.stop());
        }
    }

    @Override
    public String toString() {
        return "RestxStatsFiler";
    }
}
