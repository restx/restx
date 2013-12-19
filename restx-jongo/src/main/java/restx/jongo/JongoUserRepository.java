package restx.jongo;

import com.google.common.base.Optional;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import restx.security.CredentialsStrategy;
import restx.security.RestxPrincipal;
import restx.security.UserRepository;

/**
 */
public class JongoUserRepository<U extends RestxPrincipal> implements UserRepository<U> {
    private final JongoCollection users;
    private final JongoCollection usersCredentials;
    private final UserRefStrategy userRefStrategy;
    private final CredentialsStrategy credentialsStrategy;
    private final Class<U> userClass;
    private final U defaultAdminUser;

    public JongoUserRepository(JongoCollection users,
                               JongoCollection usersCredentials,
                               UserRefStrategy userRefStrategy,
                               CredentialsStrategy credentialsStrategy,
                               Class<U> userClass,
                               U defaultAdminUser) {
        this.users = users;
        this.usersCredentials = usersCredentials;
        this.userRefStrategy = userRefStrategy;
        this.credentialsStrategy = credentialsStrategy;
        this.userClass = userClass;
        this.defaultAdminUser = defaultAdminUser;
    }

    @Override
    public Optional<U> findUserByName(String name) {
        return Optional.fromNullable(users.get().findOne("{" + userRefStrategy.getNameProperty() + ": #}", name).as(userClass));
    }

    @Override
    public Optional<String> findCredentialByUserName(String name) {
        Optional<U> userByName = findUserByName(name);
        if (!userByName.isPresent()) {
            return Optional.absent();
        }
        UserCredentials c = findCredentialsForUserRef(userRefStrategy.getUserRef(userByName.get()));
        if (c == null) {
            return Optional.absent();
        }

        return Optional.fromNullable(c.getPasswordHash());
    }

    @Override
    public boolean isAdminDefined() {
        try {
            return users.get().count("{roles: {$all: [ # ]}}", getAdminRole()) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    protected String getAdminRole() {
        return "restx-admin";
    }

    @Override
    public U defaultAdmin() {
        return defaultAdminUser;
    }
    
    private UserCredentials findCredentialsForUserRef(String userRef) {
        return usersCredentials.get()
                .findOne("{ _id: # }", userRefStrategy.toId(userRef)).as(UserCredentials.class);
    }

    /////////////////////////////////////////////////////////
    //          repo update methods
    /////////////////////////////////////////////////////////
    public U createUser(U user) {
        users.get().save(user);
        return user;
    }

    public U updateUser(U user) {
        users.get().save(user);
        return user;
    }

    public Iterable<U> findAllUsers() {
        return users.get().find().as(userClass);
    }

    public void deleteUser(String userRef) {
        Object id = userRefStrategy.toId(userRef);
        users.get().remove("{ _id: # }", id);
        usersCredentials.get().remove("{ _id: # }", id);
    }

    public void setCredentials(String userRef, String passwordHash) {
        UserCredentials userCredentials = findCredentialsForUserRef(userRef);

        if (userCredentials == null) {
            userCredentials = new UserCredentials().setUserRef(userRef);
        }
        String hashed = credentialsStrategy.cryptCredentialsForStorage(userRef, passwordHash);
        usersCredentials.get().save(
                userCredentials
                        .setPasswordHash(hashed)
                        .setLastUpdated(DateTime.now()));
    }

    public Optional<U> findUserByKey(String key) {
        return Optional.fromNullable(users.get().findOne("{ _id: # }", userRefStrategy.toId(key)).as(userClass));
    }

    public static interface UserRefStrategy<U extends RestxPrincipal> {
        String getNameProperty();
        String getUserRef(U user);
        Object toId(String userRef);
    }
    
    public static class RefUserByNameStrategy<U extends RestxPrincipal> implements UserRefStrategy<U> {
        public String getNameProperty() {
            return "_id";
        }

        public String getUserRef(U user) {
            return user.getName();
        }

        public Object toId(String userRef) {
            return userRef;
        }
    } 
    
    public static abstract class RefUserByKeyStrategy<U extends RestxPrincipal> implements UserRefStrategy<U> {
        public String getNameProperty() {
            return "name";
        }

        public String getUserRef(U user) {
            return getId(user);
        }

        protected abstract String getId(U user);

        public Object toId(String userRef) {
            return new ObjectId(userRef);
        }
    } 
}
