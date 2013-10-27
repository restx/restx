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
public abstract class StdEntityRoute<I,O> extends StdRoute {
    private final EntityRequestBodyReader<I> entityRequestBodyReader;
    private final EntityResponseWriter<O> entityResponseWriter;
    private final RestxLogLevel logLevel;

    public StdEntityRoute(String name,
                          EntityRequestBodyReader<I> entityRequestBodyReader,
                          EntityResponseWriter<O> entityResponseWriter,
                          RestxRequestMatcher matcher,
                          HttpStatus successStatus, RestxLogLevel logLevel) {
        super(name, matcher, successStatus);
        this.entityRequestBodyReader = checkNotNull(entityRequestBodyReader);
        this.entityResponseWriter = checkNotNull(entityResponseWriter);
        this.logLevel = checkNotNull(logLevel);
    }

    @Override
    public void handle(RestxRequestMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
        resp.setLogLevel(logLevel);
        ctx.getLifecycleListener().onRouteMatch(this, req, resp);
        Optional<O> result = doRoute(req, match, entityRequestBodyReader.readBody(req, ctx));
        if (result.isPresent()) {
            entityResponseWriter.sendResponse(getSuccessStatus(), result.get(), req, resp, ctx);
        } else {
            notFound(match, resp);
        }
    }

    protected abstract Optional<O> doRoute(RestxRequest restxRequest, RestxRequestMatch match, I i) throws IOException;
}
