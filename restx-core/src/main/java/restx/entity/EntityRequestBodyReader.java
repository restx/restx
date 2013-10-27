package restx.entity;

import restx.RestxContext;
import restx.RestxRequest;

import java.io.IOException;

/**
 * Date: 23/10/13
 * Time: 19:16
 */
public interface EntityRequestBodyReader<T> {
    T readBody(RestxRequest req, RestxContext ctx) throws IOException;
}
