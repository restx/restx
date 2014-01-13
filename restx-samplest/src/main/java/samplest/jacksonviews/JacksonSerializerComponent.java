package samplest.jacksonviews;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.base.Strings;
import restx.factory.Component;

import javax.inject.Named;
import java.io.IOException;

/**
 * Demonstrates that a serializer can be a restx component.
 * Here we inject a String, but it can be any component.
 */
@Component
public class JacksonSerializerComponent extends JsonSerializer<String> {
    private final String appPackage;

    public JacksonSerializerComponent(@Named("restx.app.package") String appPackage) {
        this.appPackage = appPackage;
    }

    @Override
    public void serialize(String value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeString(appPackage + "-" + value);
    }
}
