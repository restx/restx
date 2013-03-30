package restx.jackson;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import org.jongo.marshall.jackson.id.ObjectIdDeserializer;
import org.jongo.marshall.jackson.id.ObjectIdSerializer;

/**
 * User: xavierhanin
 * Date: 3/30/13
 * Time: 5:27 PM
 */
public class FrontObjectMapperCustomizer {
    public void customize(ObjectMapper mapper) {
        mapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector() {
            @Override
            public Object findSerializer(Annotated am) {
                Object serializer = super.findSerializer(am);
                if (ObjectIdSerializer.class == serializer
                        || FixedPrecisionSerializer.class == serializer) {
                    return null;
                }
                return serializer;
            }

            @Override
            public Class<? extends JsonDeserializer<?>> findDeserializer(Annotated a) {
                Class<? extends JsonDeserializer<?>> deserializer = super.findDeserializer(a);
                if (ObjectIdDeserializer.class == deserializer
                        || FixedPrecisionDeserializer.class == deserializer) {
                    return null;
                }
                return deserializer;
            }
        });
    }
}
