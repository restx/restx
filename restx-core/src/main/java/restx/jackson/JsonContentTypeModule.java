package restx.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.base.Optional;
import restx.entity.*;
import restx.factory.Module;
import restx.factory.Provides;

import javax.inject.Named;
import java.lang.reflect.Type;
import java.util.Locale;

/**
 * Date: 27/10/13
 * Time: 14:55
 */
@Module(priority = 1000)
public class JsonContentTypeModule {
    @Provides
    public EntityDefaultContentTypeProvider jsonEntityDefaultContentTypeProvider() {
        return new EntityDefaultContentTypeProvider() {
            @Override
            public Optional<String> mayProvideDefaultContentType(Type type) {
                return Optional.of("application/json");
            }
        };
    }

    @Provides
    public EntityRequestBodyReaderFactory jsonEntityRequestBodyReaderFactory(
            @Named(FrontObjectMapperFactory.MAPPER_NAME) final ObjectMapper mapper) {
        return new EntityRequestBodyReaderFactory() {
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
        };
    }

    @Provides
    public EntityResponseWriterFactory jsonEntityResponseWriterFactory(
            @Named(FrontObjectMapperFactory.MAPPER_NAME) final ObjectMapper mapper) {
        return new EntityResponseWriterFactory() {
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
        };
    }
}
