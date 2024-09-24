package restx.jongo;

import org.joda.time.DateTime;

/**
 * User: Christophe Labouisse
 * Date: 14/06/2014
 * Time: 12:27
 */
abstract class AbstractUserCredentials implements UserCredentials {
    private String passwordHash;

    private DateTime lastUpdated;

    @Override
    public DateTime getLastUpdated() {
        return lastUpdated;
    }

    @Override
    public UserCredentials setLastUpdated(final DateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
        return this;
    }

    @Override
    public abstract UserCredentials setUserRef(final String userRef);

    @Override
    public String getPasswordHash() {
        return passwordHash;
    }

    @Override
    public UserCredentials setPasswordHash(final String passwordHash) {
        this.passwordHash = passwordHash;
        return this;
    }

    @Override
    public String toString() {
        return "UserCredentials{" +
                "userRef='" + getUserRef() + '\'' +
                ", passwordHash='XXXXXXXXXXXX'" +
                '}';
    }

    @Override
    public abstract String getUserRef();
}
