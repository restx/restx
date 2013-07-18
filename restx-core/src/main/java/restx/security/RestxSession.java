package restx.security;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.joda.time.Duration;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * RestxSession is used to store information which can be used across several HTTP requests from the same client.
 *
 * It is organized as a Map, information is stored by keys.
 *
 * It doesn't use the JEE Session mechanism, but a more lightweight system relying on a signed session cookie
 * (therefore it cannot be tampered by the client).
 *
 * The session cookie doesn't store the whole values (which could put a high load on the network and even cause
 * problems related to cookie size limit), but rather stores a a value id for each session key.
 *
 * A value id MUST identify uniquely a value when used for a given session key, and the session MUST be configured
 * with a CacheLoader per key, able to load the value corresponding to the value id for a particular key.
 *
 * Therefore on the server the session enables to access arbitrary large objects, it will only put pressure on a
 * server cache, and on cache loaders if requests are heavily distributed. Indeed the cache is not distributed,
 * so a in a large clustered environment cache miss will be very likely and cache loaders will often be called.
 * Hence in such environment you should be careful to use very efficient cache loaders if you rely heavily on
 * session.
 *
 * An example (using an arbitrary json like notation):
 * <pre>
 *     "RestxSession": {
 *          "definition": {  // this is configured once at application level
 *              "USER": (valueId) -> { return db.findOne("{_id: #}", valueId).as(User.class); }
 *          }
 *          "valueIdsByKeys": {
 *              "USER": "johndoe@acme.com" // valued from session cookie
 *          }
 *     }
 * </pre>
 * With such a restx session, when you call a #get(User.class, "USER"), the session will first check its
 * valueIdsByKeys map to find the corresponding valueId ("johndoe@acme.com"). Then it will check the cache for
 * this valueId, and in case of cache miss will use the provided cache loader which will load the user from db.
 */
public class RestxSession {
    public static class Definition {
        public static class Entry<T> {
            private final Class<T> clazz;
            private final String key;
            private final CacheLoader<String, T> loader;

            public Entry(Class<T> clazz, String key, CacheLoader<String, T> loader) {
                this.clazz = clazz;
                this.key = key;
                this.loader = loader;
            }
        }
        private final ImmutableMap<String, LoadingCache<String, ?>> caches;

        public Definition(Iterable<Entry> entries) {
            ImmutableMap.Builder<String, LoadingCache<String, ?>> builder = ImmutableMap.builder();
            for (Entry entry : entries) {
                builder.put(entry.key, CacheBuilder.newBuilder().maximumSize(1000).build(entry.loader));
            }
            caches = builder.build();
        }

        public <T> LoadingCache<String, T> getCache(Class<T> clazz, String key) {
            return (LoadingCache<String, T>) caches.get(key);
        }
    }

    private static final ThreadLocal<RestxSession> current = new ThreadLocal<>();

    static void setCurrent(RestxSession ctx) {
        if (ctx == null) {
            current.remove();
        } else {
            current.set(ctx);
        }
    }

    public static RestxSession current() {
        return current.get();
    }

    private final Definition definition;
    private final ImmutableMap<String, String> valueidsByKey;
    private final Duration expires;

    RestxSession(Definition definition, ImmutableMap<String, String> valueidsByKey, Duration expires) {
        this.definition = definition;
        this.valueidsByKey = valueidsByKey;
        this.expires = expires;
    }

    public RestxSession cleanUpCaches() {
        for (LoadingCache<String, ?> cache : definition.caches.values()) {
            cache.cleanUp();
        }
        return this;
    }


    public <T> Optional<T> get(Class<T> clazz, String key) {
        String valueid = valueidsByKey.get(key);
        if (valueid == null) {
            return Optional.absent();
        }

        try {
            return Optional.fromNullable(definition.getCache(clazz, key).get(valueid));
        } catch (ExecutionException e) {
            throw new RuntimeException(
                    "impossible to load object from cache using valueid " + valueid + " for " + key + ": " + e.getMessage(), e);
        }
    }

    public RestxSession expires(Duration duration) {
        RestxSession newCtx = new RestxSession(definition, valueidsByKey, duration);
        if (this == current()) {
            current.set(newCtx);
        }
        return newCtx;
    }

    public Duration getExpires() {
        return expires;
    }

    public <T> RestxSession define(Class<T> clazz, String key, String valueid) {
        if (!definition.caches.containsKey(key)) {
            throw new IllegalArgumentException("undefined context key: " + key + "." +
                    " Keys defined are: " + definition.caches.keySet());
        }
        // create new map by using a mutable map, not a builder, in case the the given entry overrides a previous one
        Map<String,String> newValueidsByKey = Maps.newHashMap();
        newValueidsByKey.putAll(valueidsByKey);
        if (valueid == null) {
            newValueidsByKey.remove(key);
        } else {
            newValueidsByKey.put(key, valueid);
        }
        RestxSession newCtx = new RestxSession(definition, ImmutableMap.copyOf(newValueidsByKey), expires);
        if (this == current()) {
            current.set(newCtx);
        }
        return newCtx;
    }

    ImmutableMap<String, String> valueidsByKeyMap() {
        return valueidsByKey;
    }
}
