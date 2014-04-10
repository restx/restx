package restx;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import java.io.IOException;

/**
 * Date: 19/10/13
 * Time: 21:23
 */
public final class RestxHandlerMatch {
    public static Optional<RestxHandlerMatch> of(Optional<? extends RestxRequestMatch> match, RestxHandler handler) {
        return of(match, Suppliers.ofInstance(handler));
    }

    public static Optional<RestxHandlerMatch> of(Optional<? extends RestxRequestMatch> match,
                                                 Supplier<? extends RestxHandler> handler) {
        return match.isPresent() ? Optional.of(new RestxHandlerMatch(match.get(), handler.get()))
                : Optional.<RestxHandlerMatch>absent();
    }

    private final RestxRequestMatch match;

    private final RestxHandler handler;

    public RestxHandlerMatch(RestxRequestMatch match, RestxHandler handler) {
        this.match = match;
        this.handler = handler;
    }

    public RestxRequestMatch getMatch() {
        return match;
    }

    public RestxHandler getHandler() {
        return handler;
    }

    /**
     * Handles the request match with the handler.
     *
     * This is equivalent to:
     * m.getHandler().handle(m.getRequestMatch(), req, resp, ctx)
     *
     * and is there mainly to ease calling handle without requiring to assign match in a variable.
     *
     * @param req the request to handle. It must be the request matched.
     * @param resp the response into which the request should be handled.
     * @param ctx the context which should be used to handle the request
     * @throws IOException
     */
    public void handle(RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
        handler.handle(match, req, resp, ctx);
    }

    @Override
    public String toString() {
        return "RestxHandlerMatch{" +
                "match=" + match +
                ", handler=" + handler +
                '}';
    }
}
