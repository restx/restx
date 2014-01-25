package restx.i18n;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Date: 25/1/14
 * Time: 14:37
 */
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
}
