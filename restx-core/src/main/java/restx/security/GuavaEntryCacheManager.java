package restx.security;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import restx.security.RestxSession.Definition.CachedEntry;
import restx.security.RestxSession.Definition.Entry;
import restx.security.RestxSession.Definition.EntryCacheManager;

import java.util.concurrent.ExecutionException;

/**
 * A restx session entry cache manager based on guava cache.
 *
 * You can override the cache settings by overriding the getCacheBuilder() method.
 *
 * Note that Guava Cache is not distributed, so be very careful with cache invalidation
 * when using this cache.
 *
 * This is the default EntryCacheManager, see SecurityModule which provides one.
 */
public class GuavaEntryCacheManager implements EntryCacheManager {
    @Override
    public <T> CachedEntry<T> getCachedEntry(Entry<T> entry) {
        return new GuavaCacheSessionDefinitionEntry<T>(entry.getKey(), getLoadingCacheFor(entry));
    }

    protected <T> LoadingCache<String, T> getLoadingCacheFor(final Entry<T> entry) {
        return getCacheBuilder(entry).build(getCacheLoaderFor(entry));
    }

    protected <T> CacheLoader<String, T> getCacheLoaderFor(final Entry<T> entry) {
        return new CacheLoader<String, T>() {
            @Override
            public T load(String key) throws Exception {
                return entry.getValueForId(key).orNull();
            }
        };
    }

    protected <T> CacheBuilder<Object, Object> getCacheBuilder(Entry<T> entry) {
        return CacheBuilder.newBuilder().maximumSize(1000);
    }

    /**
     * A session definition entry implementation using Guava Cache.
     */
    public static class GuavaCacheSessionDefinitionEntry<T> implements CachedEntry<T> {
        private final LoadingCache<String, T> loadingCache;
        private final String key;

        public GuavaCacheSessionDefinitionEntry(String key, LoadingCache<String, T> loadingCache) {
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
}
