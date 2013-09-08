package restx.jackson;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import restx.factory.*;

import javax.inject.Named;

/**
 * User: xavierhanin
 * Date: 1/19/13
 * Time: 12:12 AM
 */
@Module
public class FrontObjectMapperFactory {
    public static final String MAPPER_NAME = "FrontObjectMapper";
    public static final Name<ObjectMapper> NAME = Name.of(ObjectMapper.class, MAPPER_NAME);

    @Provides @Named(MAPPER_NAME)
    public ObjectMapper mapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        mapper.registerModule(new GuavaModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
