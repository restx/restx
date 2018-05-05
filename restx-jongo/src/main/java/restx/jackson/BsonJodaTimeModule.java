package restx.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import de.undercouch.bson4jackson.BsonGenerator;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.IOException;

/**
 * User: xavierhanin
 * Date: 1/24/13
 * Time: 9:03 AM
 */
@SuppressWarnings("deprecation") // DateMidnight is deprecated, but we still want to provide custom ser/deser for it
public class BsonJodaTimeModule extends SimpleModule {
    public BsonJodaTimeModule() {
        super("BsonJodaTimeModule");

        addSerializer(org.joda.time.DateMidnight.class, new JsonSerializer<DateMidnight>() {
            @Override
            public void serialize(org.joda.time.DateMidnight date, JsonGenerator gen, SerializerProvider provider)
                    throws IOException {
                if (date == null) {
                    provider.defaultSerializeNull(gen);
                } else if (gen instanceof BsonGenerator) {
                    BsonGenerator bgen = (BsonGenerator)gen;
                    bgen.writeDateTime(date.toDate());
                } else {
                    gen.writeNumber(date.getMillis());
                }
            }
        });
        addSerializer(DateTime.class, new JsonSerializer<DateTime>() {
            @Override
            public void serialize(DateTime date, JsonGenerator gen, SerializerProvider provider)
                    throws IOException {
                if (date == null) {
                    provider.defaultSerializeNull(gen);
                } else if (gen instanceof BsonGenerator) {
                    BsonGenerator bgen = (BsonGenerator)gen;
                    bgen.writeDateTime(date.toDate());
                } else {
                    gen.writeNumber(date.getMillis());
                }
            }
        });

        addDeserializer(org.joda.time.DateMidnight.class, new JsonDeserializer<org.joda.time.DateMidnight>() {
            @Override
            public org.joda.time.DateMidnight deserialize(JsonParser jp, DeserializationContext ctxt)
                    throws IOException, JsonProcessingException {
                Object date = jp.getEmbeddedObject();
                return new org.joda.time.DateMidnight(date, DateTimeZone.UTC);
            }
        });

        addDeserializer(DateTime.class, new JsonDeserializer<DateTime>() {
            @Override
            public DateTime deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
                Object date = jp.getEmbeddedObject();
                return new DateTime(date, DateTimeZone.UTC);
            }
        });
    }
}
