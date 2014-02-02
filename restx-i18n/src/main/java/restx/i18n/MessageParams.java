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

    public static MessageParams of(String k1, Object v1, String k2, Object v2) {
        return new MessageParams(ImmutableMap.of(k1, value(v1), k2, value(v2)));
    }

    public static MessageParams of(String k1, Object v1, String k2, Object v2, String k3, Object v3) {
        return new MessageParams(ImmutableMap.of(k1, value(v1), k2, value(v2), k3, value(v3)));
    }

    public static MessageParams of(String k1, Object v1, String k2, Object v2, String k3, Object v3, String k4, Object v4) {
        return new MessageParams(ImmutableMap.of(k1, value(v1), k2, value(v2), k3, value(v3), k4, value(v4)));
    }

    private static Object value(Object v) {
        return v == null ? "" : v;
    }

    public MessageParams(ImmutableMap<String, ?> map) {
        this.map = map;
    }

    private final ImmutableMap<String, ?> map;

    public MessageParams concat(String key, Object value) {
        return new MessageParams(ImmutableMap.<String, Object>builder().putAll(map).put(key, value(value)).build());
    }

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
