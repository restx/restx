package restx.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import restx.RestxLogLevel;
import restx.RestxRequest;
import restx.RestxRequestMatch;
import restx.RestxRequestMatcher;
import restx.entity.*;
import restx.http.HttpStatus;

import java.io.IOException;

public class JsonEntityRouteBuilder<I,O> extends StdEntityRoute.Builder<I,O> {
    private ObjectMapper mapper;
    public JsonEntityRouteBuilder withMapper(final ObjectMapper mapper) {
        this.mapper = mapper;
        if (entityResponseWriter == null) {
            entityResponseWriter(JsonEntityResponseWriter.<O>using(mapper));
        }
        return this;
    }

    public JsonEntityRouteBuilder havingRequestBody(Class<I> clazz) {
        if (mapper == null) {
            throw new IllegalStateException("you must give an object mapper before using this method");
        }
        entityRequestBodyReader(
                new JsonEntityRequestBodyReader(mapper.reader(clazz).withView(Views.Transient.class)));
        return this;
    }
}
