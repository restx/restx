package restx.entity;

import com.google.common.base.Optional;
import restx.factory.Component;

import java.lang.reflect.Type;

/**
 * Date: 27/10/13
 * Time: 14:31
 */
@Component
public class VoidEntityRequestBodyReaderFactory implements EntityRequestBodyReaderFactory {
    @Override
    public <T> Optional<? extends EntityRequestBodyReader<T>> mayBuildFor(Type valueType, String contentType) {
        if (valueType == Void.class || valueType == Void.TYPE) {
            return Optional.of((EntityRequestBodyReader<T>) VoidEntityRequestBodyReader.INSTANCE);
        } else {
            return Optional.absent();
        }
    }
}
