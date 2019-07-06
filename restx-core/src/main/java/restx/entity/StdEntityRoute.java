package restx.entity;

import com.google.common.base.Optional;
import restx.*;
import restx.endpoint.*;
import restx.endpoint.mappers.EndpointParameterMapper;
import restx.factory.ParamDef;
import restx.http.HttpStatus;
import restx.security.Permission;
import restx.security.PermissionFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

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
        protected EndpointParameterMapperRegistry registry;
        protected String name;
        protected Endpoint endpoint;
        protected ParamDef[] queryParameters = new ParamDef[0];
        protected HttpStatus successStatus = HttpStatus.OK;
        protected RestxLogLevel logLevel = RestxLogLevel.DEFAULT;
        protected PermissionFactory permissionFactory;
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

        public Builder<I,O> permissionFactory(final PermissionFactory permissionFactory) {
            this.permissionFactory = permissionFactory;
            return this;
        }

        public Builder<I,O> endpoint(final Endpoint endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public Builder<I,O> registry(final EndpointParameterMapperRegistry registry) {
            this.registry = registry;
            return this;
        }

        public Builder<I,O> queryParameters(final ParamDef[] queryParameters) {
            this.queryParameters = queryParameters;
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
                    endpoint, successStatus, logLevel, permissionFactory, registry, queryParameters) {
                @Override
                protected Optional<O> doRoute(RestxRequest restxRequest, RestxResponse response, RestxRequestMatch match, I i) throws IOException {
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

    private static class EndpointParameterMapperAndDef {
        EndpointParameterMapper mapper;
        EndpointParamDef endpointParamDef;

        public EndpointParameterMapperAndDef(EndpointParameterMapper mapper, EndpointParamDef endpointParamDef) {
            this.mapper = mapper;
            this.endpointParamDef = endpointParamDef;
        }
    }

    private final EntityRequestBodyReader<I> entityRequestBodyReader;
    private final EntityResponseWriter<O> entityResponseWriter;
    private final RestxLogLevel logLevel;
    private final Endpoint endpoint;
    private final PermissionFactory permissionFactory;
    private final Map<String, EndpointParameterMapperAndDef> cachedQueryParameterMappers;

    public StdEntityRoute(String name,
                          EntityRequestBodyReader<I> entityRequestBodyReader,
                          EntityResponseWriter<O> entityResponseWriter,
                          Endpoint endpoint,
                          HttpStatus successStatus,
                          RestxLogLevel logLevel,
                          PermissionFactory permissionFactory,
                          EndpointParameterMapperRegistry registry) {
        this(name, entityRequestBodyReader, entityResponseWriter, endpoint, successStatus,
                logLevel, permissionFactory, registry, new ParamDef[0]);
    }

    public StdEntityRoute(String name,
                          EntityRequestBodyReader<I> entityRequestBodyReader,
                          EntityResponseWriter<O> entityResponseWriter,
                          Endpoint endpoint,
                          HttpStatus successStatus,
                          RestxLogLevel logLevel,
                          PermissionFactory permissionFactory,
                          EndpointParameterMapperRegistry registry,
                          ParamDef[] queryParametersDefinition
    ) {
        super(name, new StdRestxRequestMatcher(endpoint), successStatus);
        this.endpoint = endpoint;
        this.permissionFactory = permissionFactory;
        this.entityRequestBodyReader = checkNotNull(entityRequestBodyReader);
        this.entityResponseWriter = checkNotNull(entityResponseWriter);
        this.logLevel = checkNotNull(logLevel);
        this.cachedQueryParameterMappers = cacheQueryParameterMappers(registry, endpoint, queryParametersDefinition);
    }

    private static Map<String, EndpointParameterMapperAndDef> cacheQueryParameterMappers(
            EndpointParameterMapperRegistry registry, Endpoint endpoint, ParamDef[] parameters) {
        Map<String, EndpointParameterMapperAndDef> cachedParameterMappers = new HashMap<>();
        for(ParamDef parameter : parameters){
            EndpointParamDef endpointParamDefDef = new EndpointParamDef(endpoint, parameter);
            cachedParameterMappers.put(
                    parameter.getName(),
                    new EndpointParameterMapperAndDef(registry.getEndpointParameterMapperFor(endpointParamDefDef), endpointParamDefDef));

        }
        return cachedParameterMappers;
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

    protected <T> T mapQueryObjectFromRequest(Class<T> targetType, String parameterName, RestxRequest request, RestxRequestMatch match, EndpointParameterKind endpointParameterKind){
        EndpointParameterMapperAndDef endpointParameterMapperAndDef = cachedQueryParameterMappers.get(parameterName);
        if(endpointParameterMapperAndDef == null) {
            throw new IllegalStateException("No cachedQueryParameterMappers for parameter "+parameterName+" : please provide corresponding ParamDef at instanciation time !");
        }

        T result = endpointParameterMapperAndDef.mapper.mapRequest(
                endpointParameterMapperAndDef.endpointParamDef,
                request, match, endpointParameterKind);

        // In case we have a null result *and* a null result default value supplier, let's use it
        if(result == null && endpointParameterMapperAndDef.endpointParamDef.isAggregateType()) {
            result = (T) endpointParameterMapperAndDef.endpointParamDef.getAggregateType().immutableEmptyInstance(endpointParameterMapperAndDef.endpointParamDef.getRawType());
        }

        return result;
    }

    @Override
    public void handle(RestxRequestMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
        RouteLifecycleListener lifecycleListener = ctx.getLifecycleListener();
        resp.setLogLevel(logLevel);

        lifecycleListener.onRouteMatch(this, req, resp);
        I input = entityRequestBodyReader.readBody(req, ctx);
        Optional<I> optionalInput = Optional.fromNullable(input);
        lifecycleListener.onEntityInput(this, req, resp, optionalInput);
        Optional<O> result = doRoute(req, resp, match, input);
        lifecycleListener.onEntityOutput(this, req, resp, optionalInput, result);
        if (result.isPresent()) {
            entityResponseWriter.sendResponse(getSuccessStatus(), result.get(), req, resp, ctx);
        } else {
            notFound(match, resp);
        }
    }

    protected abstract Optional<O> doRoute(RestxRequest restxRequest, RestxResponse restxResponse, RestxRequestMatch match, I i) throws IOException;

    // Aliases to permissionFactory allowing to have a more readable generated code through APT
    protected Permission hasRole(String role){ return permissionFactory.hasRole(role); }
    protected Permission anyOf(Permission... permissions){ return permissionFactory.anyOf(permissions); }
    protected Permission allOf(Permission... permissions){ return permissionFactory.allOf(permissions); }
    protected Permission open(){ return permissionFactory.open(); }
    protected Permission isAuthenticated(){ return permissionFactory.isAuthenticated(); }
}
