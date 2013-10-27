package restx.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.base.Optional;
import restx.entity.EntityRequestBodyReader;
import restx.entity.EntityRequestBodyReaderFactory;
import restx.entity.EntityResponseWriter;
import restx.entity.EntityResponseWriterFactory;
import restx.factory.Component;

import javax.inject.Named;
import java.lang.reflect.Type;
import java.util.Locale;

/**
 * Date: 23/10/13
 * Time: 09:47
 */
@Component
public class JsonEntityRequestBodyReaderFactory implements EntityRequestBodyReaderFactory {
    private final ObjectMapper mapper;

    public JsonEntityRequestBodyReaderFactory(
            @Named(FrontObjectMapperFactory.MAPPER_NAME) ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public <T> Optional<? extends EntityRequestBodyReader<T>> mayBuildFor(Type valueType, String contentType) {
        if (!contentType.toLowerCase(Locale.ENGLISH).startsWith("application/json")) {
            return Optional.absent();
        }
        return Optional.of(
            new JsonEntityRequestBodyReader<T>(
                mapper.readerWithView(Views.Transient.class)
                        .withType(TypeFactory.defaultInstance().constructType(valueType))))
        ;
    }
}
