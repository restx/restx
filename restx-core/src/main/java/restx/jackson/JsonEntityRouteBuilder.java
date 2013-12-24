package restx.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.base.Optional;
import restx.RestxLogLevel;
import restx.RestxRequest;
import restx.RestxRequestMatch;
import restx.RestxRequestMatcher;
import restx.entity.*;
import restx.http.HttpStatus;

import java.io.IOException;

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
