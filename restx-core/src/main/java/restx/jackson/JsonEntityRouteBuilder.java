package restx.jackson;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import restx.entity.StdEntityRoute;

import java.lang.reflect.Type;

public class JsonEntityRouteBuilder<I,O> extends StdEntityRoute.Builder<I,O> {
    public JsonEntityRouteBuilder<I,O> withObjectWriter(Type type, ObjectWriter writer) {
        entityResponseWriter(JsonEntityResponseWriter.<O>using(type, writer));
        return this;
    }

    public JsonEntityRouteBuilder<I,O> withObjectReader(Type type, ObjectReader reader) {
        entityRequestBodyReader(JsonEntityRequestBodyReader.<I>using(type, reader));
        return this;
    }

}
