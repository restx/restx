package restx.entity;

import com.google.common.base.Optional;
import restx.entity.EntityDefaultContentTypeProvider;
import restx.factory.Component;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;

/**
 * Date: 23/10/13
 * Time: 11:33
 */
@Component
public class VoidEntityDefaultContentTypeProvider implements EntityDefaultContentTypeProvider {
    private static final Collection TYPES = Arrays.asList(
            Void.class, Void.TYPE
    );

    @Override
    public Optional<String> mayProvideDefaultContentType(Type type) {
        if (TYPES.contains(type)) {
            return Optional.of("void");
        } else {
            return Optional.absent();
        }
    }
}
