package {{mainPackage}}.persistence;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.hash.Hashing;
import restx.admin.AdminModule;
import restx.factory.Component;
import restx.security.RestxPrincipal;

import javax.inject.Named;

/**
 * This is a fake, in-memory, User repository intended to provide
 * basic authentication for Hello Resource
 * In the real world, you will rely on an in-house authentication layer
 * (which will maybe ask a db for the user)
 */
@Component
public class UserRepository {

    public static class User implements RestxPrincipal {

        public static final String HELLO_ROLE = "hello-role";

        private final ImmutableSet<String> roles;
        private final String name;
        private final String passwordHash;

        public User(String name, String passwordHash, String... roles) {
            this.name = name;
            this.passwordHash = passwordHash;
            this.roles = ImmutableSet.copyOf(roles);
        }

        @Override
        public ImmutableSet<String> getPrincipalRoles() {
            return roles;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    private final ImmutableList<User> users;

    public UserRepository(@Named("restx.admin.password") String adminPassword) {
        users = ImmutableList.of(
                // admin password is injected, it is defined in AppModule
                new User("admin", hashPwd(adminPassword),
                        AdminModule.RESTX_ADMIN_ROLE, User.HELLO_ROLE),

                // define 2 other users
                new User("user1", hashPwd("user1-pwd"), User.HELLO_ROLE),
                new User("user2", hashPwd("user2-pwd"))
            );
    }

    private static String hashPwd(String pwd) {
        return Hashing.md5().hashString(pwd, Charsets.UTF_8).toString();
    }

    public Optional<? extends RestxPrincipal> findUserByName(final String name) {
        return findUserByPredicate(new Predicate<User>() {
            @Override
            public boolean apply(User input) {
                return input==null?false:input.getName().equalsIgnoreCase(name);
            }
        });
    }

    public Optional<? extends RestxPrincipal> findUserByNameAndPasswordHash(final String name, final String passwordHash) {
        return findUserByPredicate(new Predicate<User>() {
            @Override
            public boolean apply(User input) {
                return input==null?false:input.name.equalsIgnoreCase(name) && input.passwordHash.equals(passwordHash);
            }
        });
    }

    private Optional<? extends RestxPrincipal> findUserByPredicate(Predicate<User> predicate) {
        return Optional.fromNullable(Iterables.getFirst(Collections2.filter(users, predicate), null));
    }
}
