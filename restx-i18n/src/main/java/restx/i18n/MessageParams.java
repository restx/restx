package restx.i18n;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Date: 25/1/14
 * Time: 14:37
 */
@JsonSerialize(using = MessageParams.MessageParamsSerializer.class)
@JsonDeserialize(using = MessageParams.MessageParamsDeserializer.class)
public class MessageParams {
    private static final MessageParams EMPTY = new MessageParams(ImmutableMap.<String, Object>of());

    public static MessageParams empty() {
        return EMPTY;
    }

    public static MessageParams of(String k1, Object v1) {
        return new MessageParams(ImmutableMap.of(k1, value(v1)));
    }

    private static Object value(Object v) {
        return v == null ? "" : v;
    }


    public MessageParams(ImmutableMap<String, ?> map) {
        this.map = map;
    }

    private final ImmutableMap<String, ?> map;

    public Map<String, ?> toMap() {
        return map;
    }

    @Override
    public String toString() {
        return "MessageParams{" +
                "map=" + map +
                '}';
    }

    public static class MessageParamsSerializer extends JsonSerializer<MessageParams> {
        @Override
        public void serialize(MessageParams value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            provider.defaultSerializeValue(value.toMap(), jgen);
        }
    }

    public static class MessageParamsDeserializer extends JsonDeserializer<MessageParams> {
        @Override
        public MessageParams deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            JsonDeserializer<Object> mapDeser = ctxt.findRootValueDeserializer(MapType.construct(
                    LinkedHashMap.class,
                    SimpleType.construct(String.class),
                    SimpleType.construct(Object.class)));
            Map<String, Object> m = (Map<String, Object>) mapDeser.deserialize(jp, ctxt);
            return new MessageParams(ImmutableMap.copyOf(m));
        }
    }
}
