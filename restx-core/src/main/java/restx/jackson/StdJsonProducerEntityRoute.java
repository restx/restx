package restx.jackson;

import com.fasterxml.jackson.databind.ObjectWriter;
import restx.RestxLogLevel;
import restx.RestxRequestMatcher;
import restx.entity.StdEntityRoute;
import restx.entity.VoidContentTypeModule;
import restx.http.HttpStatus;
import restx.security.PermissionFactory;

import java.lang.reflect.Type;

/**
 * Date: 23/10/13
 * Time: 11:06
 */
public abstract class StdJsonProducerEntityRoute<O> extends StdEntityRoute<Void,O> {
    public StdJsonProducerEntityRoute(String name, Type type, ObjectWriter writer, RestxRequestMatcher matcher, PermissionFactory permissionFactory) {
        super(name,
                VoidContentTypeModule.VoidEntityRequestBodyReader.INSTANCE,
                JsonEntityResponseWriter.<O>using(type, writer),
                matcher,
                HttpStatus.OK, RestxLogLevel.DEFAULT, permissionFactory);
    }
}
