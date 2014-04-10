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
public class EntityResponseWriterRegistry {
    private final EntityContentTypeResolver entityContentTypeResolver;
    private final Iterable<EntityResponseWriterFactory> entityResponseWriterFactories;

    public EntityResponseWriterRegistry(Iterable<EntityDefaultContentTypeProvider> entityDefaultContentTypeProviders,
                                        Iterable<EntityResponseWriterFactory> entityResponseWriterFactories) {
        entityContentTypeResolver = new EntityContentTypeResolver(entityDefaultContentTypeProviders);
        this.entityResponseWriterFactories = entityResponseWriterFactories;
    }

    @SuppressWarnings("unchecked")
    public <T> EntityResponseWriter<T> build(final Type type, Optional<String> contentType) {
        String ct = entityContentTypeResolver.resolveContentType(type, contentType);

        for (EntityResponseWriterFactory writerFactory : entityResponseWriterFactories) {
            Optional<? extends EntityResponseWriter<Object>> writer = writerFactory.mayBuildFor(type, ct);
            if (writer.isPresent()) {
                return (EntityResponseWriter<T>) writer.get();
            }
        }

        throw new IllegalStateException(String.format(
                "no EntityResponseWriter built for %s %s.\n\n" +
                        "The list of factories are:\n%s.\n\n" +
                        "This may be because the content type '%s' is not handled by your RESTX install.\n\n" +
                        "Possible causes:\n" +
                        "  - you set the content type manually and mistyped it\n" +
                        "  - you are missing the EntityResponseWriterFactory for this content type in your classpath\n",
                type, ct, Joiner.on("\n\t").join(entityResponseWriterFactories), ct));

    }
}
