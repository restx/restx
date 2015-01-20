package restx.security;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import org.joda.time.Duration;
import restx.factory.Component;

import java.util.Map;
import java.util.Map.Entry;

/**
 * RestxSession is used to store information which can be used across several HTTP requests from the same client.
 *
 * It is organized as a Map, information is stored by keys.
 *
 * It doesn't use the JEE Session mechanism, but a more lightweight system relying on signed session data stored
 * on the client (being signed, it cannot be tampered by the client).
 *
 * The session data doesn't store the whole values (which could put a high load on the network and even cause
 * problems related to data storage limit on the client), but rather stores a a value id for each session key.
 *
 * A value id MUST identify uniquely a value when used for a given session key, and the session MUST be configured
 * with a CacheLoader per key, able to load the value corresponding to the value id for a particular key.
 *
 * Therefore on the server the session enables to access arbitrary large objects, it will only put pressure on a
 * server cache, and on cache loaders if requests are heavily distributed and depending on cache implementation.
 *
 * An example (using an arbitrary json like notation):
 * <pre>
 *     "RestxSession": {
 *          "definition": {  // this is configured once at application level
 *              "USER": (valueId) -&gt; { return db.findOne("{_id: #}", valueId).as(User.class); }
 *          }
 *          "valueIdsByKeys": {
 *              "USER": "johndoe@acme.com" // valued from client session data
 *          }
 *     }
 * </pre>
 * With such a restx session, when you call a #get(User.class, "USER"), the session will first check its
 * valueIdsByKeys map to find the corresponding valueId ("johndoe@acme.com"). Then it will check the cache for
 * this valueId, and in case of cache miss will use the provided cache loader which will load the user from db.
 *
 * If you want to define your own session keys, you should define a Definition.Entry component for it allowing
 * to load values based on their ids. You don't have to take care of caching in the Entry, caching is performed
 * by EntryCacheManager.
 */
public class RestxSession {
    @Component
    public static class Definition {
        /**
         * A session definition entry is responsible for loading session values by session value id, for a
         * particular definition key.
         *
         * They don't implement caching themselves, see CachedEntry for entries supporting caching.
         *
         * @param <T> the type of values this Entry handles.
         */
        public static interface Entry<T> {
            /**
             * Returns the definition key that this entry handles.
             * @return the definition key that this entry handles.
             */
            String getKey();

            /**
             * Gives the value corresponding to the given valueId.
             *
             * @param valueId the id of the value to get.
             *
             * @return the value, or absent if not found.
             */
            Optional<? extends T> getValueForId(String valueId);
        }

        /**
         * A cached version of session definition entry.
         *
         * This does not derive from Entry, because its its getting method does not have the same semantic as the
         * original one: here it returns a value which may not be the freshest one, while an Entry should always
         * return current one.
         *
         * CachedEntry instances are usually created from a Entry instance using a EntryCacheManager.
         *
         * @param <T> the type of values this CachedEntry handles.
         */
        public static interface CachedEntry<T> {
            /**
             * Returns the definition key that this entry handles.
             * @return the definition key that this entry handles.
             */
            String getKey();

            /**
             * Gives the value corresponding to the given valueId.
             * This value may come from a cache, so its freshness depends on configuration and implementation.
             *
             * @param valueId the id of the value to get.
             *
             * @return the value, or absent if not found.
             */
            Optional<? extends T> getValueForId(String valueId);

            /**
             * Invalidates the cache for a single value id.
             *
             * @param valueId the value id for which cache should be invalidated.
             */
            void invalidateCacheFor(String valueId);

            /**
             * Invalidates the full cache of this entry.
             *
             * This may impact more than this single entry if this entry cache is backed by a broader cache.
             */
            void invalidateCache();
        }

        /**
         * A cache manager for session definition entry.
         *
         * It transforms Entry into CachedEntry
         */
        public static interface EntryCacheManager {
            <T> CachedEntry<T> getCachedEntry(Entry<T> entry);
        }

        private final ImmutableMap<String, CachedEntry<?>> entries;

        @SuppressWarnings("unchecked") // can't use Iterable<Entry<?> as parameter in injectable constructor ATM
        public Definition(EntryCacheManager cacheManager, Iterable<Entry> entries) {
            ImmutableMap.Builder<String, CachedEntry<?>> builder = ImmutableMap.builder();
            for (Entry<?> entry : entries) {
                builder.put(entry.getKey(), cacheManager.getCachedEntry(entry));
            }
            this.entries = builder.build();
        }

        public ImmutableSet<String> entriesKeySet() {
            return entries.keySet();
        }

        public boolean hasEntryForKey(String key) {
            return entries.containsKey(key);
        }

        @SuppressWarnings("unchecked")
        public <T> Optional<CachedEntry<T>> getEntry(String key) {
            return Optional.fromNullable((CachedEntry<T>) entries.get(key));
        }

        public void invalidateAllCaches() {
            for (CachedEntry<?> entry : entries.values()) {
                entry.invalidateCache();
            }
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
    private final Optional<? extends RestxPrincipal> principal;

    RestxSession(Definition definition, ImmutableMap<String, String> valueidsByKey,
                 Optional<? extends RestxPrincipal> principal, Duration expires) {
        this.definition = definition;
        this.valueidsByKey = valueidsByKey;
        this.principal = principal;
        this.expires = expires;
    }

    public RestxSession invalidateCaches() {
        for (Entry<String, String> entry : valueidsByKey.entrySet()) {
            definition.getEntry(entry.getKey()).get().invalidateCacheFor(entry.getValue());
        }
        return this;
    }

    public <T> Optional<T> get(Class<T> clazz, String key) {
        return getValue(definition, clazz, key, valueidsByKey.get(key));
    }

    @SuppressWarnings("unchecked")
    static <T> Optional<T> getValue(Definition definition, Class<T> clazz, String key, String valueid) {
        if (valueid == null) {
            return Optional.absent();
        }

        return (Optional<T>) definition.getEntry(key).get().getValueForId(valueid);
    }

    public RestxSession expires(Duration duration) {
        return mayUpdateCurrent(new RestxSession(definition, valueidsByKey, principal, duration));
    }

    public Duration getExpires() {
        return expires;
    }

    public <T> RestxSession define(Class<T> clazz, String key, String valueid) {
        if (!definition.hasEntryForKey(key)) {
            throw new IllegalArgumentException("undefined context key: " + key + "." +
                    " Keys defined are: " + definition.entriesKeySet());
        }
        // create new map by using a mutable map, not a builder, in case the the given entry overrides a previous one
        Map<String,String> newValueidsByKey = Maps.newHashMap();
        newValueidsByKey.putAll(valueidsByKey);
        if (valueid == null) {
            newValueidsByKey.remove(key);
        } else {
            newValueidsByKey.put(key, valueid);
        }
        return mayUpdateCurrent(new RestxSession(definition, ImmutableMap.copyOf(newValueidsByKey), principal, expires));
    }

    public RestxSession authenticateAs(RestxPrincipal principal) {
        return mayUpdateCurrent(new RestxSession(definition, valueidsByKey, Optional.of(principal), expires))
                .define(RestxPrincipal.class, RestxPrincipal.SESSION_DEF_KEY, principal.getName());
    }

    public RestxSession clearPrincipal() {
        return mayUpdateCurrent(new RestxSession(definition, valueidsByKey, Optional.<RestxPrincipal>absent(), expires))
                .define(RestxPrincipal.class, RestxPrincipal.SESSION_DEF_KEY, null);
    }

    public Optional<? extends RestxPrincipal> getPrincipal() {
        return principal;
    }

    private RestxSession mayUpdateCurrent(RestxSession newSession) {
        if (this == current()) {
            current.set(newSession);
        }
        return newSession;
    }

    ImmutableMap<String, String> valueidsByKeyMap() {
        return valueidsByKey;
    }

    /**
     * Executes a runnable with this session set as current session.
     *
     * Inside the runnable, the current session can be accessed with RestxSession.current().
     *
     * This method takes care of restoring the current session after the call. So if the current session
     * is altered inside the runnable it won't have effect on the caller.
     *
     * @param runnable the runnable to execute.
     */
    public void runIn(Runnable runnable) {
        RestxSession current = current();

        setCurrent(this);
        try {
            runnable.run();
        } finally {
            setCurrent(current);
        }
    }
}
