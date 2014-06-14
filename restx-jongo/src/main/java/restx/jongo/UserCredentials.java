package restx.jongo;

import org.joda.time.DateTime;
import org.jongo.marshall.jackson.oid.Id;

/**
 */
public class UserCredentials {
    @Id
    private String userRef;

    private String passwordHash;

    private DateTime lastUpdated;

    public DateTime getLastUpdated() {
        return lastUpdated;
    }

    public UserCredentials setLastUpdated(final DateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
        return this;
    }

    public String getUserRef() {
        return userRef;
    }

    public UserCredentials setUserRef(final String userRef) {
        this.userRef = userRef;
        return this;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserCredentials setPasswordHash(final String passwordHash) {
        this.passwordHash = passwordHash;
        return this;
    }

    @Override
    public String toString() {
        return "UserCredentials{" +
                "userRef='" + userRef + '\'' +
                ", passwordHash='XXXXXXXXXXXX'" +
                '}';
    }
}
