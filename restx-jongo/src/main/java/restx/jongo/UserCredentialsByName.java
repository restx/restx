package restx.jongo;

import org.jongo.marshall.jackson.oid.Id;

/**
 */
public class UserCredentialsByName extends AbstractUserCredentials implements UserCredentials {
    @Id
    private String userRef;

    @Override
    public String getUserRef() {
        return userRef;
    }

    @Override
    public UserCredentials setUserRef(final String userRef) {
        this.userRef = userRef;
        return this;
    }
}
