package restx.entity;

import com.google.common.base.Optional;
import org.apache.commons.io.IOUtils;
import restx.RestxContext;
import restx.RestxRequest;
import restx.RestxResponse;
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
    public EntityRequestBodyReaderFactory testEntityRequestBodyReaderFactory() {
        return new EntityRequestBodyReaderFactory() {
            @Override
            public Optional<? extends EntityRequestBodyReader> mayBuildFor(Type valueType, String contentType) {
                if (!contentType.toLowerCase(Locale.ENGLISH).startsWith("text/plain")) {
                    return Optional.absent();
                }

                return Optional.of(
                    new EntityRequestBodyReader<String>() {

                        @Override
                        public Type getType() {
                            return String.class;
                        }

                        @Override
                        public String readBody(RestxRequest req, RestxContext ctx) throws IOException {
                            String body;
                            try {
                                body = IOUtils.toString(req.getContentStream(), "UTF8");
                            } finally {
                                IOUtils.closeQuietly(req.getContentStream());
                            }
                            return body;
                        }
                    }
                );
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

                return Optional.of(new AbstractEntityResponseWriter<T>(valueType, "text/plain") {
                    @Override
                    protected void write(T value, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
                        resp.getWriter().append(value.toString());
                    }
                });
            }
        };
    }
}
