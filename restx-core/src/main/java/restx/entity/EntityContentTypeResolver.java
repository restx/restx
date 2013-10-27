package restx.entity;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;

import java.lang.reflect.Type;

/**
 * Date: 23/10/13
 * Time: 09:53
 */
public class EntityContentTypeResolver {
    private final Iterable<EntityDefaultContentTypeProvider> entityDefaultContentTypeProviders;

    public EntityContentTypeResolver(Iterable<EntityDefaultContentTypeProvider> entityDefaultContentTypeProviders) {
        this.entityDefaultContentTypeProviders = entityDefaultContentTypeProviders;
    }

    public String resolveContentType(final Type type, Optional<String> contentType) {
        return contentType.or(new Supplier<String>() {
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
    }
}
