package restx.entity;

import com.google.common.base.Optional;
import restx.factory.Module;
import restx.factory.Provides;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;

/**
 * Date: 27/10/13
 * Time: 15:01
 */
@Module
public class VoidContentTypeModule {
    private static final Collection TYPES = Arrays.asList(
            Void.class, Void.TYPE
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
                if (valueType == Void.class || valueType == Void.TYPE) {
                    return Optional.of((EntityRequestBodyReader<T>) VoidEntityRequestBodyReader.INSTANCE);
                } else {
                    return Optional.absent();
                }
            }
        };
    }
}
