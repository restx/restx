package restx.entity;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Set;

/**
 * Date: 23/10/13
 * Time: 09:53
 */
public class EntityResponseWriterRegistry {
    private final Iterable<EntityDefaultContentTypeProvider> entityDefaultContentTypeProviders;
    private final Iterable<EntityResponseWriterFactory> entityResponseWriterFactories;

    public EntityResponseWriterRegistry(Iterable<EntityDefaultContentTypeProvider> entityDefaultContentTypeProviders,
                                        Iterable<EntityResponseWriterFactory> entityResponseWriterFactories) {
        this.entityDefaultContentTypeProviders = entityDefaultContentTypeProviders;
        this.entityResponseWriterFactories = entityResponseWriterFactories;
    }

    public <T> EntityResponseWriter<T> build(final Type type, Optional<String> contentType) {
        String ct = contentType.or(new Supplier<String>() {
            @Override
            public String get() {
                for (EntityDefaultContentTypeProvider entityDefaultContentTypeProvider : entityDefaultContentTypeProviders) {
                    Optional<String> contentType = entityDefaultContentTypeProvider.mayProvideDefaultContentType(type);
                    if (contentType.isPresent()) {
                        return contentType.get();
                    }
                }
                throw new IllegalStateException(String.format(
                        "no EntityDefaultContentTypeProvider provided for %s.\n\n" +
                                "The list of providers are:\n%s.\n\n" +
                                "By default RESTX should provide a 'application/json' default content type for any type\n" +
                                "through JsonEntityDefaultContentTypeProvider, if you haven't removed it on purpose you\n" +
                                "may have to double check your restx distribution and your classpath\n",
                        type, Joiner.on("\n\t").join(entityDefaultContentTypeProviders)));
            }
        });

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
