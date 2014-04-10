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

    public SimpleUserRepository(Class<U> userClass, U defaultAdmin) {
        this.userClass = userClass;
        this.defaultAdmin = defaultAdmin;
    }

    @Override
    public Optional<U> findUserByName(String name) {
        return Optional.fromNullable("admin".equals(name) ? defaultAdmin : null);
    }

    @Override
    public Optional<String> findCredentialByUserName(String userName) {
        //User admin / admin
        return Optional.fromNullable("admin".equals(userName) ? "$2a$10$QL7OBVj/qpEdWKQNTXp12.08e3AR3szAzTZOCRlAPS2BOIaYXESrW" : null);
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
