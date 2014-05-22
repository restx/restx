package restx.jackson;

import com.fasterxml.jackson.databind.ObjectWriter;
import restx.entity.AbstractEntityResponseWriter;
import restx.RestxContext;
import restx.RestxRequest;
import restx.RestxResponse;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Date: 23/10/13
 * Time: 09:07
 */
public class JsonEntityResponseWriter<T> extends AbstractEntityResponseWriter<T> {
    public static <T> JsonEntityResponseWriter<T> using(Type type, ObjectWriter writer) {
        return new JsonEntityResponseWriter<>(type, writer);
    }

    protected final ObjectWriter writer;

    private JsonEntityResponseWriter(Type type, ObjectWriter writer) {
        super(type, "application/json");
        this.writer = writer;
    }

    @Override
    protected void write(T value, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
        writer.writeValue(resp.getWriter(), value);
    }
}
