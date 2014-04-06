package restx.security;

import com.google.common.base.Optional;

/**
 * Date: 14/12/13
 * Time: 14:48
 */
public interface UserRepository<U extends RestxPrincipal> {
    /**
     * Finds a user by name in the repository.
     *
     * @param name user name
     * @return Optional user
     */
    Optional<U> findUserByName(String name);

    /**
     * Finds a user credentials by userName
     *
     * @param name user name
     * @return Optional credential
     */
    Optional<String> findCredentialByUserName(String name);

    /**
     * Tells wether a restx admin is defined in this repository.
     * This allows to access restx admin console even when the user repository is empty or does
     * not contain a restx admin.
     *
     * Returning true will simply bypass the default admin behavior, and thus prevent any connection to the admin
     * console if no restx admin user is actually present in the UserRepository.
     *
     * Note that default admin password check is performed based on a string injected password hash which is not
     * very secured.
     *
     * @return true is a restx admin is defined in this repo.
     */
    boolean isAdminDefined();

    /**
     * Returns the default admin to use if none is defined and authentication succeeds.
     *
     * Note that implementation must return a value even if they always return true on isAdminDefined(), because the
     * default admin user name is checked before isAdminDefined() because in most implementations returning defaultAdmin
     * is less expensive than checking if admin is defined.
     *
     * @return the default restx admin user. See restx-admin module for details on restx admin.
     */
    U defaultAdmin();
}
