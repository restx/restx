package restx.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import restx.*;
import restx.entity.EntityResponseWriter;
import restx.http.HttpStatus;
import restx.jackson.JsonEntityRequestBodyReader;
import restx.jackson.JsonEntityResponseWriter;
import restx.jackson.Views;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: xavierhanin
 * Date: 1/19/13
 * Time: 8:10 AM
 */
public abstract class StdEntityRoute<I,O> extends StdRoute {
    public static class Builder<I,O> {
        protected EntityRequestBodyReader entityRequestBodyReader
                = VoidContentTypeModule.VoidEntityRequestBodyReader.INSTANCE;
        protected EntityResponseWriter<O> entityResponseWriter;
        protected String name;
        protected RestxRequestMatcher matcher;
        protected HttpStatus successStatus = HttpStatus.OK;
        protected RestxLogLevel logLevel = RestxLogLevel.DEFAULT;
        protected MatchedEntityRoute<I,O> matchedEntityRoute;

        public Builder entityRequestBodyReader(final EntityRequestBodyReader<I> entityRequestBodyReader) {
            this.entityRequestBodyReader = entityRequestBodyReader;
            return this;
        }

        public Builder entityResponseWriter(final EntityResponseWriter<O> entityResponseWriter) {
            this.entityResponseWriter = entityResponseWriter;
            return this;
        }

        public Builder name(final String name) {
            this.name = name;
            return this;
        }

        public Builder matcher(final RestxRequestMatcher matcher) {
            this.matcher = matcher;
            return this;
        }

        public Builder successStatus(final HttpStatus successStatus) {
            this.successStatus = successStatus;
            return this;
        }

        public Builder logLevel(final RestxLogLevel logLevel) {
            this.logLevel = logLevel;
            return this;
        }

        public Builder matchedEntityRoute(final MatchedEntityRoute<I, O> matchedEntityRoute) {
            this.matchedEntityRoute = matchedEntityRoute;
            return this;
        }

        public StdEntityRoute<I,O> build() {
            checkNotNull(matchedEntityRoute, "you must provide a matchedEntityRoute");
            return new StdEntityRoute<I, O>(
                    name, entityRequestBodyReader, entityResponseWriter,
                    matcher, successStatus, logLevel) {
                @Override
                protected Optional<O> doRoute(RestxRequest restxRequest, RestxRequestMatch match, I i) throws IOException {
                    return matchedEntityRoute.route(restxRequest, match, i);
                }
            };
        }
    }

    public static <I,O> Builder<I,O> builder() {
        return new Builder<>();
    }

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
