package restx;

import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.factory.Component;

import java.io.IOException;

/**
 * Automatically closes the response if no exception occurs.
 *
 * It doesn't close the response if an exception occur, because exception handling in higher order filters
 * may want to write to response, and RESTX main router does also.
 */
@Component(priority = 10000)
public class ResponseCloserFilter implements RestxFilter, RestxHandler {
    private static final Logger logger = LoggerFactory.getLogger(ResponseCloserFilter.class);

    @Override
    public Optional<RestxHandlerMatch> match(RestxRequest req) {
        return Optional.of(new RestxHandlerMatch(new StdRestxRequestMatch(req.getRestxPath()), this));
    }

    @Override
    public void handle(RestxRequestMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx)
            throws IOException {
        ctx.nextHandlerMatch().handle(req, resp, ctx);
        try {
            resp.close();
        } catch (Exception e) {
            logger.error("ERROR while closing response: " + e.getMessage(), e);
        }
    }
}
