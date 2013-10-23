package restx.entity;

import com.google.common.base.Optional;
import restx.*;
import restx.entity.EntityResponseWriter;
import restx.http.HttpStatus;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: xavierhanin
 * Date: 1/19/13
 * Time: 8:10 AM
 */
public abstract class StdEntityRoute<T> extends StdRoute {
    private final EntityResponseWriter<T> entityResponseWriter;
    private final RestxLogLevel logLevel;

    public StdEntityRoute(String name, EntityResponseWriter<T> entityResponseWriter, RestxRequestMatcher matcher,
                          HttpStatus successStatus, RestxLogLevel logLevel) {
        super(name, matcher, successStatus);
        this.entityResponseWriter = checkNotNull(entityResponseWriter);
        this.logLevel = checkNotNull(logLevel);
    }

    @Override
    public void handle(RestxRequestMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
        resp.setLogLevel(logLevel);
        ctx.getLifecycleListener().onRouteMatch(this, req, resp);
        Optional<T> result = doRoute(req, match);
        if (result.isPresent()) {
            entityResponseWriter.sendResponse(getSuccessStatus(), (T) result.get(), req, resp, ctx);
        } else {
            notFound(match, resp);
        }
    }

    protected abstract Optional<T> doRoute(RestxRequest restxRequest, RestxRequestMatch match) throws IOException;
}
