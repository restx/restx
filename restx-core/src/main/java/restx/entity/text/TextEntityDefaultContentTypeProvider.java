package restx.entity.text;

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
public class TextEntityDefaultContentTypeProvider implements EntityDefaultContentTypeProvider {
    private static final Collection TEXT_TYPES = Arrays.asList(
            String.class, StringBuilder.class, StringBuffer.class
    );

    @Override
    public Optional<String> mayProvideDefaultContentType(Type type) {
        if (TEXT_TYPES.contains(type)) {
            return Optional.of("text/plain");
        } else {
            return Optional.absent();
        }
    }
}
