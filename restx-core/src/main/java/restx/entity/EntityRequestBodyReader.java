package restx.entity;

import restx.RestxContext;
import restx.RestxRequest;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Date: 23/10/13
 * Time: 19:16
 */
public interface EntityRequestBodyReader<T> {
    /**
     * The type of T that this reader is handling.
     *
     * @return type of T that this reader is handling.
     */
    Type getType();

    /**
     * Reads the body of the request and convert it into a T.
     *
     * @param req the request to read the body from
     * @param ctx current context
     * @return an instance of T
     * @throws IOException on IO errors
     */
    T readBody(RestxRequest req, RestxContext ctx) throws IOException;
}
