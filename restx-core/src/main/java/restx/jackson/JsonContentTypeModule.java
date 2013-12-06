package restx.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(JsonContentTypeModule.class);

    private static final String JACKSON_VIEW_PARAMETER = "view=";

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
                Class<?> clazz = getCTJacksonViewClass(valueType, contentType, Views.Transient.class);
                return Optional.of(
                        new JsonEntityRequestBodyReader<T>(
                                mapper.readerWithView(clazz)
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
                Class<?> clazz = getCTJacksonViewClass(valueType, contentType, Views.Transient.class);
                return Optional.of(
                        new JsonEntityResponseWriter<T>(
                                mapper.writerWithView(clazz)
                                        .withType(TypeFactory.defaultInstance().constructType(valueType))))
                        ;
            }
        };
    }

    private Class<?> getCTJacksonViewClass(Type valueType, String contentType, Class<?> defaultClazz) {
        int parameterIndex = contentType.indexOf(JACKSON_VIEW_PARAMETER);
        if (parameterIndex != -1) {
            String className = contentType.substring(parameterIndex + JACKSON_VIEW_PARAMETER.length());
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                logger.error("The Jackson view class '{}' was not found while marshalling type '{}' " +
                        "(content-type : '{}')", className, valueType, contentType);
                throw new IllegalStateException(e);
            }
        }
        return defaultClazz;
    }
}
