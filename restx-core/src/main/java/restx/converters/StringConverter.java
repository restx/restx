package restx.converters;

import com.google.common.base.Function;
import com.google.common.base.Optional;

/**
 * User: xavierhanin
 * Date: 2/5/13
 * Time: 11:15 PM
 */
public interface StringConverter<T> {
    Optional<? extends Function<String, T>> accept(Class clazz);
}
