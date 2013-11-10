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
    public static <T> JsonEntityResponseWriter<T> using(ObjectMapper mapper) {
        return new JsonEntityResponseWriter(mapper.writer().withView(Views.Transient.class));
    }

    protected final ObjectWriter writer;

    public JsonEntityResponseWriter(ObjectWriter writer) {
        super("application/json");
        this.writer = writer;
    }

    @Override
    protected void write(T value, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
        Object v = value;
        if (v instanceof Iterable) {
            v = Lists.newArrayList((Iterable) value);
        }
        writer.writeValue(resp.getWriter(), v);
    }
}
