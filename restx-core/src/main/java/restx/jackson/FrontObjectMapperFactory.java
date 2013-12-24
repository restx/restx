package restx.jackson;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import restx.AppSettings;
import restx.RestxContext;
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
    public static final String WRITER_NAME = "FrontObjectWriter";
    public static final String READER_NAME = "FrontObjectReader";

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

    @Provides @Named(WRITER_NAME)
    public ObjectWriter objectWriter(@Named(FrontObjectMapperFactory.MAPPER_NAME) final ObjectMapper mapper,
                                     AppSettings appSettings) {
        ObjectWriter objectWriter = RestxContext.Modes.PROD.equals(appSettings.mode())
                ? mapper.writer() : mapper.writerWithDefaultPrettyPrinter();
        return objectWriter.withView(Views.Transient.class);
    }

    @Provides @Named(READER_NAME)
    public ObjectReader objectReader(@Named(FrontObjectMapperFactory.MAPPER_NAME) final ObjectMapper mapper) {
        return mapper.readerWithView(Views.Transient.class);
    }
}
