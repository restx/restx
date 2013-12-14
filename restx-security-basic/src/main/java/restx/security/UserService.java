package restx.security;

import com.google.common.base.Optional;

/**
 * Date: 14/12/13
 * Time: 15:08
 */
public interface UserService<U extends RestxPrincipal> {
    /**
     * Finds a user by name in the repository.
     *
     * @param name
     * @return
     */
    Optional<U> findUserByName(String name);

    /**
     * Finds a user by name in repository and check its passwordHash
     *
     * @param name
     * @param passwordHash
     * @return
     */
    Optional<U> findAndCheckCredentials(String name, String passwordHash);

}
