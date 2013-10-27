package restx.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.common.collect.Lists;
import restx.RestxContext;
import restx.RestxRequest;
import restx.RestxResponse;
import restx.entity.EntityRequestBodyReader;

import java.io.IOException;

/**
 * Date: 23/10/13
 * Time: 09:07
 */
public class JsonEntityRequestBodyReader<T> implements EntityRequestBodyReader<T> {
    public static <T> JsonEntityRequestBodyReader<T> using(ObjectMapper mapper) {
        return new JsonEntityRequestBodyReader(mapper.reader().withView(Views.Transient.class));
    }

    protected final ObjectReader reader;

    public JsonEntityRequestBodyReader(ObjectReader reader) {
        this.reader = reader;
    }

    @Override
    public T readBody(RestxRequest req, RestxContext ctx) throws IOException {
        return reader.readValue(req.getContentStream());
    }
}
