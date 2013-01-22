package restx;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/**
 * User: xavierhanin
 * Date: 1/19/13
 * Time: 12:12 AM
 */
@Module
public class RestxCoreModule {
    @Provides @Singleton ObjectMapper mapper() {
        ObjectMapper mapper = new ObjectMapper();
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
