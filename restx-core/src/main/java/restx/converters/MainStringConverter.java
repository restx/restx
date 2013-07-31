package restx.converters;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Primitives;
import restx.factory.*;

/**
 * User: xavierhanin
 * Date: 2/5/13
 * Time: 11:18 PM
 */
public class MainStringConverter {
    private final ImmutableMap<Class, StringConverter> converters;

    public MainStringConverter(Iterable<StringConverter> stringConverters) {
        ImmutableMap.Builder<Class, StringConverter> builder = ImmutableMap.builder();
        for (StringConverter converter : stringConverters) {
            builder.put(Primitives.wrap(converter.getConvertedClass()), converter);
        }

        converters = builder.build();
    }

    public <T> Optional<T> convert(Optional<String> value, Class<T> toClass) {
        if(value.isPresent()){
            return Optional.of(convert(value.get(), toClass));
        } else {
            return Optional.absent();
        }
    }

    public <T> T convert(String value, Class<T> toClass) {
        StringConverter converter = converters.get(Primitives.wrap(toClass));
        if (converter == null) {
            throw new IllegalArgumentException(String.format(
                    "no converter registered for %s. Converters are registered for: %s",
                    toClass.getName(), converters.keySet()));
        }
        return (T) converter.apply(value);
    }

}
