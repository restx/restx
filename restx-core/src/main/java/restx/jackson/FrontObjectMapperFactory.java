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
        return new ObjectMapper()
                .registerModule(new JodaModule())
                .registerModule(new GuavaModule())
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.EAGER_DESERIALIZER_FETCH)
                .disable(SerializationFeature.EAGER_SERIALIZER_FETCH)
                ;
    }
}
