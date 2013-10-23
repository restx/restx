package restx.jackson;

import com.google.common.base.Optional;
import restx.entity.EntityDefaultContentTypeProvider;
import restx.factory.Component;

import java.lang.reflect.Type;

/**
 * Date: 23/10/13
 * Time: 10:15
 */
@Component(priority = 1000)
public class JsonEntityDefaultContentTypeProvider implements EntityDefaultContentTypeProvider {
    @Override
    public Optional<String> mayProvideDefaultContentType(Type type) {
        return Optional.of("application/json");
    }
}
