package restx.jackson;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import restx.factory.*;

/**
 * User: xavierhanin
 * Date: 1/19/13
 * Time: 12:12 AM
 */
public class FrontObjectMapperFactory extends SingleNameFactoryMachine<ObjectMapper> {
    public static final Name<ObjectMapper> NAME = Name.of(ObjectMapper.class, "FrontObjectMapper");

    public FrontObjectMapperFactory() {
        super(0, new NoDepsMachineEngine<ObjectMapper>(NAME, BoundlessComponentBox.FACTORY) {
            @Override
            public ObjectMapper doNewComponent(SatisfiedBOM satisfiedBOM) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JodaModule());
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                return mapper;
            }
        });
    }
}
