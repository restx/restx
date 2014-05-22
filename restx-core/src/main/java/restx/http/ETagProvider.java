package restx.http;

import restx.http.ETag;

/**
 * An ETag provider is used to provide an ETag for an entity.
 *
 * It is used by the default restx cache filter to check and set ETag on returned entities.
 */
public interface ETagProvider<T> {
    /**
     * The type of Entity that this provider can handle.
     *
     * @return the type of Entity that this provider can handle.
     */
    Class<T> getEntityType();

    /**
     * Provides an ETag for the given entity.
     *
     * @param entity the entity for which ETag should be provided.
     * @return the ETag
     */
    ETag provideETagFor(T entity);
}
