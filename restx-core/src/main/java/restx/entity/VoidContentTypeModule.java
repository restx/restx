package restx.entity;

import com.google.common.base.Optional;
import restx.RestxContext;
import restx.RestxRequest;
import restx.RestxResponse;
import restx.factory.Module;
import restx.factory.Provides;
import restx.http.HttpStatus;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

/**
 * Date: 27/10/13
 * Time: 15:01
 */
@Module
public class VoidContentTypeModule {
    private static final Collection TYPES = Arrays.asList(
            Void.class, Void.TYPE, Empty.class
    );

    @Provides
    public EntityDefaultContentTypeProvider voidEntityDefaultContentTypeProvider() {
        return new EntityDefaultContentTypeProvider() {
            @Override
            public Optional<String> mayProvideDefaultContentType(Type type) {
                if (TYPES.contains(type)) {
                    return Optional.of("void");
                } else {
                    return Optional.absent();
                }
            }
        };
    }

    @Provides
    public EntityRequestBodyReaderFactory voidEntityRequestBodyReaderFactory() {
        return new EntityRequestBodyReaderFactory() {
            @Override
            public <T> Optional<? extends EntityRequestBodyReader<T>> mayBuildFor(Type valueType, String contentType) {
                if (!contentType.toLowerCase(Locale.ENGLISH).equals("void")) {
                    return Optional.absent();
                }
                return Optional.of((EntityRequestBodyReader<T>) VoidEntityRequestBodyReader.INSTANCE);
            }
        };
    }

    @Provides
    public EntityResponseWriterFactory voidEntityResponseWriterFactory() {
        return new EntityResponseWriterFactory() {
            @Override
            public <T> Optional<? extends EntityResponseWriter<T>> mayBuildFor(Type valueType, String contentType) {
                if (!contentType.toLowerCase(Locale.ENGLISH).equals("void")) {
                    return Optional.absent();
                }

                return Optional.of(new EntityResponseWriter<T>() {
                    @Override
                    public void sendResponse(HttpStatus status, T value, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
                        resp.setStatus(status == HttpStatus.OK ? HttpStatus.NO_CONTENT : status);
                        ctx.getLifecycleListener().onBeforeWriteContent(req, resp);
                        ctx.getLifecycleListener().onAfterWriteContent(req, resp);
                    }
                });
            }
        };
    }

    public static class VoidEntityRequestBodyReader implements EntityRequestBodyReader<Void> {
        public static final VoidEntityRequestBodyReader INSTANCE = new VoidEntityRequestBodyReader();

        @Override
        public Void readBody(RestxRequest req, RestxContext ctx) throws IOException {
            return null;
        }
    }
}
