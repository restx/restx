package restx.converters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import restx.factory.Component;
import restx.jackson.FrontObjectMapperFactory;

import javax.inject.Named;

/**
 * User: xavierhanin
 * Date: 2/5/13
 * Time: 11:18 PM
 */
@Component
public class MainStringConverter {
    private final ObjectMapper mapper;

    public MainStringConverter(@Named(FrontObjectMapperFactory.MAPPER_NAME) ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public <T> Optional<T> convert(Optional<String> value, Class<T> toClass) {
        if(value.isPresent()){
            return Optional.of(convert(value.get(), toClass));
        } else {
            return Optional.absent();
        }
    }

    public <T> T convert(String value, Class<T> toClass) {
        return mapper.convertValue(value, toClass);
    }
}
