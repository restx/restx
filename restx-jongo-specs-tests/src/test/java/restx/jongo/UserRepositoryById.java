package restx.jongo;

import restx.security.CredentialsStrategy;

/**
 * User: Christophe Labouisse
 * Date: 12/06/2014
 * Time: 12:30
 */
public class UserRepositoryById extends JongoUserRepository<JongoUserById> {

    public UserRepositoryById(JongoCollection users, JongoCollection usersCredentials, CredentialsStrategy credentialsStrategy) {
        super(
                users, usersCredentials,
                new JongoUserRepository.RefUserByKeyStrategy<JongoUserById>() {
                    @Override
                    protected String getId(JongoUserById user) {
                        return user.getId();
                    }
                },
                credentialsStrategy,
                JongoUserById.class,
                new JongoUserById(null, "restx-admin", "*")
        );
    }
}
