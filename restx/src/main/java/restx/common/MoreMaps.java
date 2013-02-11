package restx.common;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

/**
 * User: xavierhanin
 * Date: 2/11/13
 * Time: 12:30 PM
 */
public class MoreMaps {

    public static <K, V> Builder<K, V> immutableBuilder() {
        return new Builder<>();
    }

    public static class Builder<K, V> extends ImmutableMap.Builder<K, V> {
        public Builder<K, V> putIfPresent(K key, Optional<V> value) {
            if (value.isPresent()) {
                put(key, value.get());
            }
            return this;
        }
    }
}
