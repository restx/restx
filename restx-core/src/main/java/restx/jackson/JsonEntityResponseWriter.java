package restx.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.collect.Lists;
import restx.entity.AbstractEntityResponseWriter;
import restx.entity.EntityResponseWriter;
import restx.RestxContext;
import restx.RestxRequest;
import restx.RestxResponse;
import restx.http.HttpStatus;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Date: 23/10/13
 * Time: 09:07
 */
public class JsonEntityResponseWriter<T> extends AbstractEntityResponseWriter<T> {
    public static <T> JsonEntityResponseWriter<T> using(ObjectWriter writer) {
        return new JsonEntityResponseWriter<>(writer);
    }

    protected final ObjectWriter writer;

    private JsonEntityResponseWriter(ObjectWriter writer) {
        super("application/json");
        this.writer = writer;
    }

    @Override
    protected void write(T value, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
        writer.writeValue(resp.getWriter(), value);
    }
}
