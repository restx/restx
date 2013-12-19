package restx.security;

import com.google.common.base.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Date: 14/12/13
 * Time: 15:09
 */
public class StdUserService<U extends RestxPrincipal> implements UserService<U> {
    private final UserRepository<U> repository;
    private final CredentialsStrategy checker;
    private final String defaultAdminPasswordHash;

    public StdUserService(UserRepository<U> repository,
                          CredentialsStrategy checker,
                          String defaultAdminPasswordHash) {
        this.repository = checkNotNull(repository);
        this.checker = checkNotNull(checker);
        this.defaultAdminPasswordHash = checkNotNull(defaultAdminPasswordHash);
    }

    @Override
    public Optional<U> findUserByName(String name) {
        Optional<U> user = repository.findUserByName(name);
        if (!user.isPresent()) {
            U defaultAdmin = repository.defaultAdmin();
            if (defaultAdmin.getName().equals(name) && !repository.isAdminDefined()) {
                return Optional.of(defaultAdmin);
            }
        }
        return user;
    }

    @Override
    public Optional<U> findAndCheckCredentials(String name, String passwordHash) {
        Optional<U> user = findUserByName(name);
        if (!user.isPresent()) {
            return Optional.absent();
        }

        Optional<String> credential = repository.findCredentialByUserName(name);
        if (!credential.isPresent()) {
            if (repository.defaultAdmin() == user.get()) {
                if (defaultAdminPasswordHash.equals(passwordHash)) {
                    return user;
                }
            }

            return Optional.absent();
        }

        if (checker.checkCredentials(name, passwordHash, credential.get())) {
            return user;
        } else {
            return Optional.absent();
        }
    }
}
