package restx.entity;

import com.google.common.base.Optional;

import java.lang.reflect.Type;

/**
 * Date: 23/10/13
 * Time: 09:40
 */
public interface EntityRequestBodyReaderFactory {
    <T> Optional<? extends EntityRequestBodyReader<T>> mayBuildFor(Type valueType, String contentType);
}
