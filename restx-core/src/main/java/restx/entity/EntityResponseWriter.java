package restx.entity;

import restx.RestxContext;
import restx.RestxRequest;
import restx.RestxResponse;
import restx.http.HttpStatus;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Date: 23/10/13
 * Time: 08:59
 */
public interface EntityResponseWriter<T> {
    /**
     * The type of T that this reader is handling.
     *
     * @return type of T that this reader is handling.
     */
    Type getType();

    /**
     * Writes a T into a RestxResponse.
     *
     * @param status the HttpStatus of the current response
     * @param value the T value to write to the response
     * @param req the original request
     * @param resp the repsonse to write to
     * @param ctx the current context
     * @throws IOException in case of IO error
     */
    void sendResponse(HttpStatus status, T value, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException;
}
