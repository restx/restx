package restx.jackson;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import restx.entity.StdEntityRoute;

public class JsonEntityRouteBuilder<I,O> extends StdEntityRoute.Builder<I,O> {
    public JsonEntityRouteBuilder<I,O> withObjectWriter(ObjectWriter writer) {
        entityResponseWriter(JsonEntityResponseWriter.<O>using(writer));
        return this;
    }

    public JsonEntityRouteBuilder<I,O> withObjectReader(ObjectReader reader) {
        entityRequestBodyReader(JsonEntityRequestBodyReader.<I>using(reader));
        return this;
    }

}
