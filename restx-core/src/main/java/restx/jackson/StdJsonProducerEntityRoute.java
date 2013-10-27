package restx.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import restx.RestxLogLevel;
import restx.RestxRequestMatcher;
import restx.entity.StdEntityRoute;
import restx.entity.VoidEntityRequestBodyReader;
import restx.http.HttpStatus;

/**
 * Date: 23/10/13
 * Time: 11:06
 */
public abstract class StdJsonProducerEntityRoute<O> extends StdEntityRoute<Void,O> {
    public StdJsonProducerEntityRoute(String name, ObjectMapper mapper, RestxRequestMatcher matcher) {
        super(name,
                VoidEntityRequestBodyReader.INSTANCE,
                JsonEntityResponseWriter.<O>using(mapper),
                matcher,
                HttpStatus.OK, RestxLogLevel.DEFAULT);
    }
}
