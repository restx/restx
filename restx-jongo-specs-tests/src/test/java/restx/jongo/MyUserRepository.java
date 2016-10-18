package restx.jongo;

import restx.security.CredentialsStrategy;

/**
 * User: Christophe Labouisse
 * Date: 12/06/2014
 * Time: 12:30
 */
public class MyUserRepository extends JongoUserRepository<JongoUser> {

    public MyUserRepository(JongoCollection users, JongoCollection usersCredentials, UserRefStrategy<JongoUser> userRefStrategy, CredentialsStrategy credentialsStrategy, JongoUser defaultAdminUser) {
        super(users, usersCredentials, userRefStrategy, credentialsStrategy, JongoUser.class, defaultAdminUser);
    }
}
