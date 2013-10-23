package restx.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.base.Optional;
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
public class JsonEntityResponseWriterFactory implements EntityResponseWriterFactory {
    private final ObjectMapper mapper;

    public JsonEntityResponseWriterFactory(
            @Named(FrontObjectMapperFactory.MAPPER_NAME) ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public <T> Optional<? extends EntityResponseWriter<T>> mayBuildFor(Type valueType, String contentType) {
        if (!contentType.toLowerCase(Locale.ENGLISH).startsWith("application/json")) {
            return Optional.absent();
        }
        return Optional.of(
            new JsonEntityResponseWriter<T>(
                mapper.writerWithView(Views.Transient.class)
                        .withType(TypeFactory.defaultInstance().constructType(valueType))))
        ;
    }
}
