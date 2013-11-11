package restx.entity;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import restx.factory.Component;

import java.lang.reflect.Type;

/**
 * Date: 23/10/13
 * Time: 09:53
 */
@Component
public class EntityRequestBodyReaderRegistry {
    private final EntityContentTypeResolver entityContentTypeResolver;
    private final Iterable<EntityRequestBodyReaderFactory> entityRequestBodyReaderFactories;

    public EntityRequestBodyReaderRegistry(Iterable<EntityDefaultContentTypeProvider> entityDefaultContentTypeProviders,
                                           Iterable<EntityRequestBodyReaderFactory> entityRequestBodyReaderFactories) {
        entityContentTypeResolver = new EntityContentTypeResolver(entityDefaultContentTypeProviders);
        this.entityRequestBodyReaderFactories = entityRequestBodyReaderFactories;
    }

    public <T> EntityRequestBodyReader<T> build(final Type type, Optional<String> contentType) {
        String ct = entityContentTypeResolver.resolveContentType(type, contentType);

        for (EntityRequestBodyReaderFactory readerFactory : entityRequestBodyReaderFactories) {
            Optional<? extends EntityRequestBodyReader<Object>> reader = readerFactory.mayBuildFor(type, ct);
            if (reader.isPresent()) {
                return (EntityRequestBodyReader<T>) reader.get();
            }
        }

        throw new IllegalStateException(String.format(
                "no EntityRequestBodyReader built for %s %s.\n\n" +
                        "The list of factories are:\n%s.\n\n" +
                        "This may be because the content type '%s' is not handled by your RESTX install.\n\n" +
                        "Possible causes:\n" +
                        "  - you set the content type manually and mistyped it\n" +
                        "  - you are missing the EntityRequestBodyReaderFactory for this content type in your classpath\n",
                type, ct, Joiner.on("\n\t").join(entityRequestBodyReaderFactories), ct));

    }
}
