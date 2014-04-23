package restx.security;

import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Simple UserRepository implementation
 *
 */
public class SimpleUserRepository<U extends RestxPrincipal> implements UserRepository<U> {
    private static final Logger logger = LoggerFactory.getLogger(SimpleUserRepository.class);

    private final Class<U> userClass;
    private final U defaultAdmin;
    private String adminUsername;
    private String adminCredentials;

    public SimpleUserRepository(Class<U> userClass, U defaultAdmin, String adminUsername, String adminCredentials) {
        this.userClass = userClass;
        this.defaultAdmin = defaultAdmin;
        this.adminUsername = adminUsername;
        this.adminCredentials = adminCredentials;
    }

    @Override
    public Optional<U> findUserByName(String name) {
        return Optional.fromNullable(adminUsername.equals(name) ? defaultAdmin : null);
    }

    @Override
    public Optional<String> findCredentialByUserName(String userName) {
        //User admin / admin
        return Optional.fromNullable(adminUsername.equals(userName) ? adminCredentials : null);
    }

    @Override
    public boolean isAdminDefined() {
        return true;
    }

    @Override
    public U defaultAdmin() {
        return defaultAdmin;
    }

}
