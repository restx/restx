package restx.entity;

import restx.RestxContext;
import restx.RestxRequest;
import restx.RestxResponse;
import restx.http.HttpStatus;

import java.io.IOException;

/**
 * Date: 23/10/13
 * Time: 08:59
 */
public interface EntityResponseWriter<T> {
    void sendResponse(HttpStatus status, T value, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException;
}
