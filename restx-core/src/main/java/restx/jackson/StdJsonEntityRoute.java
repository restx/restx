package restx.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import restx.RestxLogLevel;
import restx.RestxRequestMatcher;
import restx.entity.EntityResponseWriter;
import restx.entity.StdEntityRoute;
import restx.http.HttpStatus;

/**
 * Date: 23/10/13
 * Time: 11:06
 */
public abstract class StdJsonEntityRoute<T> extends StdEntityRoute<T> {
    public StdJsonEntityRoute(String name, ObjectMapper mapper, RestxRequestMatcher matcher) {
        super(name, JsonEntityResponseWriter.<T>using(mapper), matcher,
                HttpStatus.OK, RestxLogLevel.DEFAULT);
    }
}
