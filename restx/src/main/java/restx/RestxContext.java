package restx;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * User: xavierhanin
 * Date: 1/30/13
 * Time: 5:20 PM
 */
public class RestxContext {
    public static class Definition {
        public static class Entry<T> {
            private final Class<T> clazz;
            private final String name;
            private final CacheLoader<String, T> loader;

            public Entry(Class<T> clazz, String name, CacheLoader<String, T> loader) {
                this.clazz = clazz;
                this.name = name;
                this.loader = loader;
            }
        }
        private final ImmutableMap<String, LoadingCache<String, ?>> caches;

        public Definition(Iterable<Entry> entries) {
            ImmutableMap.Builder<String, LoadingCache<String, ?>> builder = ImmutableMap.builder();
            for (Entry entry : entries) {
                builder.put(entry.name, CacheBuilder.newBuilder().maximumSize(1000).build(entry.loader));
            }
            caches = builder.build();
        }

        public <T> LoadingCache<String, T> getCache(Class<T> clazz, String name) {
            return (LoadingCache<String, T>) caches.get(name);
        }
    }

    private static final ThreadLocal<RestxContext> current = new ThreadLocal<>();

    static void setCurrent(RestxContext ctx) {
        current.set(ctx);
    }

    public static RestxContext current() {
        return current.get();
    }

    private final Definition definition;
    private final ImmutableMap<String, String> keysByName;

    RestxContext(Definition definition, ImmutableMap<String, String> keysByName) {
        this.definition = definition;
        this.keysByName = keysByName;
    }

    public <T> Optional<T> get(Class<T> clazz, String name) {
        String key = keysByName.get(name);
        if (key == null) {
            return Optional.absent();
        }

        try {
            return Optional.fromNullable(definition.getCache(clazz, name).get(key));
        } catch (ExecutionException e) {
            throw new RuntimeException(
                    "impossible to load object from cache using key " + key + " for " + name + ": " + e.getMessage(), e);
        }
    }

    public <T> RestxContext define(Class<T> clazz, String name, String key) {
        if (!definition.caches.containsKey(name)) {
            throw new IllegalArgumentException("undefined context name: " + name + "." +
                    " Names defined are: " + definition.caches.keySet());
        }
        // create new map by using a mutable map, not a builder, in case the the given entry overrides a previous one
        Map<String,String> newKeysByName = Maps.newHashMap();
        newKeysByName.putAll(keysByName);
        newKeysByName.put(name, key);
        RestxContext newCtx = new RestxContext(definition, ImmutableMap.copyOf(newKeysByName));
        if (this == current()) {
            current.set(newCtx);
        }
        return newCtx;
    }

    ImmutableMap<String, String> keysByNameMap() {
        return keysByName;
    }
}
