package samplest.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import samplest.domain.Car;

import java.io.IOException;

/**
 * User: eoriou
 * Date: 04/12/2013
 * Time: 14:04
 */
public class CustomJacksonSerializer extends JsonSerializer<Car> {

    @Override
    public void serialize(Car value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        if (provider.getConfig().getActiveView() == Views.Frontal.Details.class) {
            jgen.writeString("{'status' : 'ok'}");
        } else {
            jgen.writeString("{'status' : 'ko'}");
        }
    }
}
