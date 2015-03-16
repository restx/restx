package restx.jackson;

import com.fasterxml.jackson.databind.ObjectWriter;
import restx.RestxLogLevel;
import restx.endpoint.Endpoint;
import restx.endpoint.EndpointParameterMapperRegistry;
import restx.entity.StdEntityRoute;
import restx.entity.VoidContentTypeModule;
import restx.factory.ParamDef;
import restx.http.HttpStatus;
import restx.security.PermissionFactory;

import java.lang.reflect.Type;

/**
 * Date: 23/10/13
 * Time: 11:06
 */
public abstract class StdJsonProducerEntityRoute<O> extends StdEntityRoute<Void,O> {
    public StdJsonProducerEntityRoute(
            String name, Type type, ObjectWriter writer,
            Endpoint endpoint, PermissionFactory permissionFactory,
            EndpointParameterMapperRegistry endpointParameterMapperRegistry
    ) {
        super(name,
                VoidContentTypeModule.VoidEntityRequestBodyReader.INSTANCE,
                JsonEntityResponseWriter.<O>using(type, writer),
                endpoint,
                HttpStatus.OK, RestxLogLevel.DEFAULT,
                permissionFactory,
                endpointParameterMapperRegistry);
    }

    public StdJsonProducerEntityRoute(
            String name, Type type, ObjectWriter writer,
            Endpoint endpoint, PermissionFactory permissionFactory,
            EndpointParameterMapperRegistry endpointParameterMapperRegistry,
            ParamDef[] queryParametersDef
    ) {
        super(name,
                VoidContentTypeModule.VoidEntityRequestBodyReader.INSTANCE,
                JsonEntityResponseWriter.<O>using(type, writer),
                endpoint,
                HttpStatus.OK, RestxLogLevel.DEFAULT,
                permissionFactory,
                endpointParameterMapperRegistry, queryParametersDef);
    }
}
