package restx.jackson;

import com.fasterxml.jackson.databind.ObjectReader;
import restx.RestxContext;
import restx.RestxRequest;
import restx.entity.EntityRequestBodyReader;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Date: 23/10/13
 * Time: 09:07
 */
public class JsonEntityRequestBodyReader<T> implements EntityRequestBodyReader<T> {
    public static <T> JsonEntityRequestBodyReader<T> using(Type type, ObjectReader reader) {
        return new JsonEntityRequestBodyReader<>(type, reader);
    }

    private final Type type;
    protected final ObjectReader reader;

    private JsonEntityRequestBodyReader(Type type, ObjectReader reader) {
        this.type = type;
        this.reader = reader;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public T readBody(RestxRequest req, RestxContext ctx) throws IOException {
        return reader.readValue(req.getContentStream());
    }
}
