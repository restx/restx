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
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;

public class BsonJSR310Module extends SimpleModule {
    public BsonJSR310Module() {
        super("BsonJSR310Module");

        addSerializer(Instant.class, instantSerializer());
        addDeserializer(Instant.class, instantDeserializer());

        addSerializer(LocalDate.class, localDateSerializer());
        addDeserializer(LocalDate.class, localDateDeserializer());
    }

    public static JsonDeserializer<LocalDate> localDateDeserializer() {
        return new JsonDeserializer<LocalDate>() {
            @Override
            public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
                Date date = (Date) p.getEmbeddedObject();
                return date.toInstant().atZone(ZoneOffset.UTC).toLocalDate();
            }
        };
    }

    public static JsonSerializer<LocalDate> localDateSerializer() {
        return new JsonSerializer<LocalDate>() {
            @Override
            public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                if (value == null) {
                    serializers.defaultSerializeNull(gen);
                } else if (gen instanceof BsonGenerator) {
                    BsonGenerator bgen = (BsonGenerator)gen;
                    bgen.writeDateTime(java.sql.Date.from(value.atStartOfDay(ZoneOffset.UTC).toInstant()));
                } else {
                    gen.writeString(value.toString());
                }
            }
        };
    }

    public static JsonDeserializer<Instant> instantDeserializer() {
        return new JsonDeserializer<Instant>() {
            @Override
            public Instant deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
                Date date = (Date) jp.getEmbeddedObject();
                return date.toInstant();
            }
        };
    }

    public static JsonSerializer<Instant> instantSerializer() {
        return new JsonSerializer<Instant>() {
            @Override
            public void serialize(Instant date, JsonGenerator gen, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
                if (date == null) {
                    serializerProvider.defaultSerializeNull(gen);
                } else if (gen instanceof BsonGenerator) {
                    BsonGenerator bgen = (BsonGenerator)gen;
                    bgen.writeDateTime(Date.from(date));
                } else {
                    gen.writeString(date.toString());
                }
            }
        };
    }
}