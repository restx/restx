package restx.jackson;

import com.fasterxml.jackson.databind.ObjectReader;
import restx.RestxContext;
import restx.RestxRequest;
import restx.entity.EntityRequestBodyReader;

import java.io.IOException;

/**
 * Date: 23/10/13
 * Time: 09:07
 */
public class JsonEntityRequestBodyReader<T> implements EntityRequestBodyReader<T> {
    public static <T> JsonEntityRequestBodyReader<T> using(ObjectReader reader) {
        return new JsonEntityRequestBodyReader<>(reader);
    }

    protected final ObjectReader reader;

    private JsonEntityRequestBodyReader(ObjectReader reader) {
        this.reader = reader;
    }

    @Override
    public T readBody(RestxRequest req, RestxContext ctx) throws IOException {
        return reader.readValue(req.getContentStream());
    }
}
