package restx.converters;

import com.google.common.base.Function;

/**
 * User: xavierhanin
 * Date: 2/5/13
 * Time: 11:15 PM
 */
public interface StringConverter<T> extends Function<String, T> {
    Class<T> getConvertedClass();
}
