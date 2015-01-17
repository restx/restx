package restx.security;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import restx.security.RestxSession.Definition.Entry;

import java.util.concurrent.ExecutionException;

/**
 * A session definition entry implementation using Guava Cache.
 *
 * Note that Guava Cache is not distributed, so be very careful with cache invalidation
 * when using this cache.
 */
public class GuavaCacheSessionDefinitionEntry<T> implements Entry<T> {
    private final LoadingCache<String, T> loadingCache;
    private final Class<T> clazz;
    private final String key;

    public GuavaCacheSessionDefinitionEntry(Class<T> clazz, String key, CacheLoader<String, T> loader) {
        this(clazz, key, CacheBuilder.newBuilder().maximumSize(1000).build(loader));
    }

    public GuavaCacheSessionDefinitionEntry(Class<T> clazz, String key, LoadingCache<String, T> loadingCache) {
        this.clazz = clazz;
        this.key = key;
        this.loadingCache = loadingCache;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Optional<T> getValueForId(String valueId) {
        try {
            return Optional.fromNullable(loadingCache.get(valueId));
        } catch (CacheLoader.InvalidCacheLoadException e) {
            // this exception is raised when cache loader returns null, which may happen if the object behind the key
            // is deleted. Therefore we just return an absent value
            return Optional.absent();
        } catch (ExecutionException e) {
            throw new RuntimeException(
                    "impossible to load object from cache using valueid " + valueId + " for " + key + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void invalidateCacheFor(String valueId) {
        loadingCache.invalidate(valueId);
    }

    @Override
    public void invalidateCache() {
        loadingCache.invalidateAll();
    }
}
