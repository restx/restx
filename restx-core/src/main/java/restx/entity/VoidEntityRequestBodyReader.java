package restx.entity;

import restx.RestxContext;
import restx.RestxRequest;

import java.io.IOException;

/**
 * Date: 23/10/13
 * Time: 19:27
 */
public class VoidEntityRequestBodyReader implements EntityRequestBodyReader<Void> {
    public static final VoidEntityRequestBodyReader INSTANCE = new VoidEntityRequestBodyReader();

    @Override
    public Void readBody(RestxRequest req, RestxContext ctx) throws IOException {
        return null;
    }
}
