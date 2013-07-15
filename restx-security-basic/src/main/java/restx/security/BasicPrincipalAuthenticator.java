package restx.security;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

/**
 * Basic authenticator.
 */
public interface BasicPrincipalAuthenticator {
    /**
     * Finds a principal by name.
     *
     * This is used to load principals in the cache.
     *
     * @param name the principal name
     * @return the principal, absent if not found
     */
    Optional<? extends RestxPrincipal> findByName(String name);

    /**
     * Authenticates a principal by name and passwordHash.
     * <p>
     * Note that usually the passwordHash has been hashed on the client with a weak hashing function like md5,
     * therefore it is strongly recommended to hash it again with salt and a strong hashing function like bcrypt.
     * </p>
     * <p>
     * Read http://codingkilledthecat.wordpress.com/2012/09/04/some-best-practices-for-web-app-authentication/ for some
     * best practices about password authentication.
     * </p>
     * <p>
     * this method may choose to throw WebException when authentication fails to give additional details, or simply
     * return an absent principal.
     * </p>
     *
     * @param name         the principal name
     * @param passwordHash the provided password hash (default is md5)
     * @param principalData any additional data provided during authentication
     * @return the authenticated principal if authentication is valid, an absent principal otherwise.
     */
    Optional<? extends RestxPrincipal> authenticate(String name, String passwordHash, ImmutableMap<String, ?> principalData);
}
