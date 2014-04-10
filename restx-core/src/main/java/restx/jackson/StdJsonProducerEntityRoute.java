package restx.jackson;

import com.fasterxml.jackson.databind.ObjectWriter;
import restx.RestxLogLevel;
import restx.RestxRequestMatcher;
import restx.entity.StdEntityRoute;
import restx.entity.VoidContentTypeModule;
import restx.http.HttpStatus;

/**
 * Date: 23/10/13
 * Time: 11:06
 */
public abstract class StdJsonProducerEntityRoute<O> extends StdEntityRoute<Void,O> {
    public StdJsonProducerEntityRoute(String name, ObjectWriter writer, RestxRequestMatcher matcher) {
        super(name,
                VoidContentTypeModule.VoidEntityRequestBodyReader.INSTANCE,
                JsonEntityResponseWriter.<O>using(writer),
                matcher,
                HttpStatus.OK, RestxLogLevel.DEFAULT);
    }
}
