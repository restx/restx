package restx.entity;

import com.google.common.base.Optional;
import restx.RestxContext;
import restx.RestxRequest;
import restx.RestxResponse;
import restx.entity.AbstractEntityResponseWriter;
import restx.entity.EntityDefaultContentTypeProvider;
import restx.entity.EntityResponseWriter;
import restx.entity.EntityResponseWriterFactory;
import restx.factory.Module;
import restx.factory.Provides;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

/**
 * Date: 27/10/13
 * Time: 15:05
 */
@Module
public class TextContentTypeModule {
    private static final Collection TEXT_TYPES = Arrays.asList(
            String.class, StringBuilder.class, StringBuffer.class
    );

    @Provides
    public EntityDefaultContentTypeProvider textEntityDefaultContentTypeProvider() {
        return new EntityDefaultContentTypeProvider() {
            @Override
            public Optional<String> mayProvideDefaultContentType(Type type) {
                if (TEXT_TYPES.contains(type)) {
                    return Optional.of("text/plain");
                } else {
                    return Optional.absent();
                }
            }
        };
    }

    @Provides
    public EntityResponseWriterFactory textEntityResponseWriterFactory() {
        return new EntityResponseWriterFactory() {
            @Override
            public <T> Optional<? extends EntityResponseWriter<T>> mayBuildFor(Type valueType, String contentType) {
                if (!contentType.toLowerCase(Locale.ENGLISH).startsWith("text/plain")) {
                    return Optional.absent();
                }

                return Optional.of(new AbstractEntityResponseWriter<T>("text/plain") {
                    @Override
                    protected void write(T value, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
                        resp.getWriter().append(value.toString());
                    }
                });
            }
        };
    }
}
