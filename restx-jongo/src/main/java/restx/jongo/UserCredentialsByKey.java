package restx.jongo;

import org.jongo.marshall.jackson.oid.Id;
import org.jongo.marshall.jackson.oid.ObjectId;

/**
 * User: Christophe Labouisse
 * Date: 14/06/2014
 * Time: 12:26
 */
public class UserCredentialsByKey extends AbstractUserCredentials implements UserCredentials {
    @Id
    @ObjectId
    private String userRef;

    @Override
    public String getUserRef() {
        return userRef;
    }

    @Override
    public UserCredentials setUserRef(String userRef) {
        this.userRef = userRef;
        return this;
    }
}
