package restx.jongo;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import de.undercouch.bson4jackson.BsonGenerator;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;

public class BsonJSR310Module extends SimpleModule {
    public BsonJSR310Module() {
        super("BsonJSR310Module");

        addSerializer(Instant.class, new JsonSerializer<Instant>() {
            @Override
            public void serialize(Instant date, JsonGenerator gen, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
                if (date == null) {
                    serializerProvider.defaultSerializeNull(gen);
                } else if (gen instanceof BsonGenerator) {
                    BsonGenerator bgen = (BsonGenerator)gen;
                    bgen.writeDateTime(new Date(date.toEpochMilli()));
                } else {
                    gen.writeString(date.toString());
                }
            }
        });

        addDeserializer(Instant.class, new JsonDeserializer<Instant>() {
            @Override
            public Instant deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
                Date date = (Date) jp.getEmbeddedObject();
                return Instant.ofEpochMilli(date.getTime());
            }
        });
    }
}