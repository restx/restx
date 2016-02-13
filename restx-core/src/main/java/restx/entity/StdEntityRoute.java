package restx.entity;

import com.google.common.base.Optional;
import restx.RestxContext;
import restx.RestxLogLevel;
import restx.RestxRequest;
import restx.RestxRequestMatch;
import restx.RestxRequestMatcher;
import restx.RestxResponse;
import restx.RouteLifecycleListener;
import restx.StdRoute;
import restx.http.HttpStatus;
import restx.security.Permission;
import restx.security.PermissionFactory;

import java.io.IOException;
import java.lang.reflect.Type;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: xavierhanin
 * Date: 1/19/13
 * Time: 8:10 AM
 */
public abstract class StdEntityRoute<I,O> extends StdRoute {

    public static class Builder<I,O> {
        protected EntityRequestBodyReader<I> entityRequestBodyReader;
        protected EntityResponseWriter<O> entityResponseWriter;
        protected String name;
        protected RestxRequestMatcher matcher;
        protected HttpStatus successStatus = HttpStatus.OK;
        protected RestxLogLevel logLevel = RestxLogLevel.DEFAULT;
        protected MatchedEntityRoute<I,O> matchedEntityRoute;

        public Builder<I,O> entityRequestBodyReader(final EntityRequestBodyReader<I> entityRequestBodyReader) {
            this.entityRequestBodyReader = entityRequestBodyReader;
            return this;
        }

        public Builder<I,O> entityResponseWriter(final EntityResponseWriter<O> entityResponseWriter) {
            this.entityResponseWriter = entityResponseWriter;
            return this;
        }

        public Builder<I,O> name(final String name) {
            this.name = name;
            return this;
        }

        public Builder<I,O> matcher(final RestxRequestMatcher matcher) {
            this.matcher = matcher;
            return this;
        }

        public Builder<I,O> successStatus(final HttpStatus successStatus) {
            this.successStatus = successStatus;
            return this;
        }

        public Builder<I,O> logLevel(final RestxLogLevel logLevel) {
            this.logLevel = logLevel;
            return this;
        }

        public Builder<I,O> matchedEntityRoute(final MatchedEntityRoute<I, O> matchedEntityRoute) {
            this.matchedEntityRoute = matchedEntityRoute;
            return this;
        }

        public StdEntityRoute<I,O> build() {
            checkNotNull(matchedEntityRoute, "you must provide a matchedEntityRoute");
            return new StdEntityRoute<I, O>(
                    name, entityRequestBodyReader == null ? voidBodyReader() : entityRequestBodyReader,
                    entityResponseWriter,
                    matcher, successStatus, logLevel) {
                @Override
                protected Optional<O> doRoute(RestxRequest restxRequest, RestxRequestMatch match, I i) throws IOException {
                    return matchedEntityRoute.route(restxRequest, match, i);
                }
            };
        }

        /*
           We want to give a default value to entityRequestBodyReader to void.
           It would be better to do that only if I is Void, but generics aren't reified so we can't check that.
           So we need to cast it to I, without really knowing if I is Void.
         */
        @SuppressWarnings("unchecked")
        private EntityRequestBodyReader<I> voidBodyReader() {
            return (EntityRequestBodyReader<I>) VoidContentTypeModule.VoidEntityRequestBodyReader.INSTANCE;
        }
    }

    public static <I,O> Builder<I,O> builder() {
        return new Builder<>();
    }

    private final EntityRequestBodyReader<I> entityRequestBodyReader;
    private final EntityResponseWriter<O> entityResponseWriter;
    private final RestxLogLevel logLevel;
    private final PermissionFactory permissionFactory;

    public StdEntityRoute(String name,
                          EntityRequestBodyReader<I> entityRequestBodyReader,
                          EntityResponseWriter<O> entityResponseWriter,
                          RestxRequestMatcher matcher,
                          HttpStatus successStatus,
                          RestxLogLevel logLevel
    ) {
        this(name, entityRequestBodyReader, entityResponseWriter, matcher, successStatus, logLevel, null);
    }

    public StdEntityRoute(String name,
                          EntityRequestBodyReader<I> entityRequestBodyReader,
                          EntityResponseWriter<O> entityResponseWriter,
                          RestxRequestMatcher matcher,
                          HttpStatus successStatus,
                          RestxLogLevel logLevel,
                          PermissionFactory permissionFactory
    ) {
        super(name, matcher, successStatus);
        this.permissionFactory = permissionFactory;
        this.entityRequestBodyReader = checkNotNull(entityRequestBodyReader);
        this.entityResponseWriter = checkNotNull(entityResponseWriter);
        this.logLevel = checkNotNull(logLevel);
    }

    /**
     * The Java type of I, the entity into which request body will be unmarshalled.
     *
     * @return I type
     */
    public Type getEntityRequestBodyType() {
        return entityRequestBodyReader.getType();
    }

    /**
     * The Java type of O, the entity from which response body will be marshalled.
     *
     * @return O type
     */
    public Type getEntityResponseType() {
        return entityResponseWriter.getType();
    }

    @Override
    public void handle(RestxRequestMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
        RouteLifecycleListener lifecycleListener = ctx.getLifecycleListener();
        resp.setLogLevel(logLevel);

        lifecycleListener.onRouteMatch(this, req, resp);
        I input = entityRequestBodyReader.readBody(req, ctx);
        Optional<I> optionalInput = Optional.fromNullable(input);
        lifecycleListener.onEntityInput(this, req, resp, optionalInput);
        Optional<O> result = doRoute(req, match, input);
        lifecycleListener.onEntityOutput(this, req, resp, optionalInput, result);
        if (result.isPresent()) {
            entityResponseWriter.sendResponse(getSuccessStatus(), result.get(), req, resp, ctx);
        } else {
            notFound(match, resp);
        }
    }

    protected abstract Optional<O> doRoute(RestxRequest restxRequest, RestxRequestMatch match, I i) throws IOException;

    // Aliases to permissionFactory allowing to have a more readable generated code through APT
    protected Permission hasRole(String role){ return permissionFactory.hasRole(role); }
    protected Permission anyOf(Permission... permissions){ return permissionFactory.anyOf(permissions); }
    protected Permission allOf(Permission... permissions){ return permissionFactory.allOf(permissions); }
    protected Permission open(){ return permissionFactory.open(); }
    protected Permission isAuthenticated(){ return permissionFactory.isAuthenticated(); }
}
