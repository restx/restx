package restx.converters;

import com.google.common.base.Function;
import com.google.common.base.Optional;

/**
 * User: xavierhanin
 * Date: 2/5/13
 * Time: 11:54 PM
 */
public class SimpleStringConverter<T> implements StringConverter<T> {
    protected final Class<T> convertedClass;
    private Function<String, T> converter;

    protected SimpleStringConverter(Class<T> convertedClass, Function<String, T> converter) {
        this.convertedClass = convertedClass;
        this.converter = converter;
    }

    @Override
    public Optional<? extends Function<String, T>> accept(Class clazz) {
        if(convertedClass.isAssignableFrom(clazz)){
            return Optional.of(converterFor(clazz));
        } else {
            return Optional.absent();
        }
    }

    protected Function<String, T> converterFor(Class clazz) {
        return converter;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SimpleStringConverter{");
        sb.append("convertedClass=").append(convertedClass);
        sb.append(", converter=").append(converter);
        sb.append('}');
        return sb.toString();
    }
}
