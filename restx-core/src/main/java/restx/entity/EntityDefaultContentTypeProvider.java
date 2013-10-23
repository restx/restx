package restx.entity;

import com.google.common.base.Optional;

import java.lang.reflect.Type;

/**
 * Date: 23/10/13
 * Time: 09:50
 */
public interface EntityDefaultContentTypeProvider {
    Optional<String> mayProvideDefaultContentType(Type type);
}
