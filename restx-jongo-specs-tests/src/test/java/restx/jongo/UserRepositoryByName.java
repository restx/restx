package restx.jongo;

import restx.security.CredentialsStrategy;

/**
 * User: Christophe Labouisse
 * Date: 12/06/2014
 * Time: 12:30
 */
public class UserRepositoryByName extends JongoUserRepository<JongoUserByName> {

    public UserRepositoryByName(JongoCollection users, JongoCollection usersCredentials, CredentialsStrategy credentialsStrategy) {
        super(
                users, usersCredentials,
                new RefUserByNameStrategy<JongoUserByName>(),
                credentialsStrategy,
                JongoUserByName.class,
                new JongoUserByName(null, "restx-admin", "*")
        );
    }
}
