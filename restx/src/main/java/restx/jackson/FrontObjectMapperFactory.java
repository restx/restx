package restx.jackson;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import restx.factory.BoundlessComponentBox;
import restx.factory.Factory;
import restx.factory.Name;
import restx.factory.SingleNameFactoryMachine;

/**
 * User: xavierhanin
 * Date: 1/19/13
 * Time: 12:12 AM
 */
public class FrontObjectMapperFactory extends SingleNameFactoryMachine<ObjectMapper> {
    public static final Name<ObjectMapper> NAME = Name.of(ObjectMapper.class, "FrontObjectMapper");

    public FrontObjectMapperFactory() {
        super(0, NAME, BoundlessComponentBox.FACTORY);
    }

    @Override
    protected ObjectMapper doNewComponent(Factory factory) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector() {
            @Override
            public Object findSerializer(Annotated am) {
                Object serializer = super.findSerializer(am);
                if (serializer instanceof Class
                        && "org.jongo.marshall.jackson.id.ObjectIdSerializer".equals(((Class) serializer).getName())) {
                    return null;
                }
                return serializer;
            }

            @Override
            public Class<? extends JsonDeserializer<?>> findDeserializer(Annotated a) {
                Class<? extends JsonDeserializer<?>> deserializer = super.findDeserializer(a);
                if (deserializer != null && "org.jongo.marshall.jackson.id.ObjectIdDeserializer".equals(deserializer.getName())) {
                    return null;
                }
                return deserializer;
            }
        });
        return mapper;
    }
}
