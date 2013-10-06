package restx.converters;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Primitives;

/**
 * User: xavierhanin
 * Date: 2/5/13
 * Time: 11:18 PM
 */
public class MainStringConverter {
    private final Iterable<StringConverter> converters;

    public MainStringConverter(Iterable<StringConverter> stringConverters) {
        this.converters = ImmutableList.copyOf(stringConverters);
    }

    public <T> Optional<T> convert(Optional<String> value, Class<T> toClass) {
        if(value.isPresent()){
            return Optional.of(convert(value.get(), toClass));
        } else {
            return Optional.absent();
        }
    }

    public <T> T convert(String value, Class<T> toClass) {
        for(StringConverter converter : converters){
            Optional<? extends Function<String, T>> potentialTransformer = converter.accept(Primitives.wrap(toClass));
            if(potentialTransformer.isPresent()) {
                return (T) potentialTransformer.get().apply(value);
            }
        }

        throw new IllegalArgumentException(String.format(
            "No converter registered for %s. Converters are registered for: %s",
            toClass.getName(), converters.toString()));
    }

}
