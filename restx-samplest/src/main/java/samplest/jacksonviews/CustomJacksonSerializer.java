package samplest.jacksonviews;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.base.Strings;
import samplest.domain.Car;

import java.io.IOException;

/**
 * User: eoriou
 * Date: 04/12/2013
 * Time: 14:04
 */
public class CustomJacksonSerializer extends JsonSerializer<String> {

    @Override
    public void serialize(String value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject();
        jgen.writeObjectField("status", Strings.isNullOrEmpty(value) ? "ko" : "ok");
        if (provider.getConfig().getActiveView() == Views.Details.class) {
            jgen.writeObjectField("details", value);
        }
        jgen.writeEndObject();
    }
}
