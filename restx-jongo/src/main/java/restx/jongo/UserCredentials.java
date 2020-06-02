package restx.jongo;

import org.joda.time.DateTime;

/**
 * User: Christophe Labouisse
 * Date: 14/06/2014
 * Time: 12:25
 */
public interface UserCredentials {
    DateTime getLastUpdated();

    UserCredentials setLastUpdated(DateTime lastUpdated);

    String getUserRef();

    UserCredentials setUserRef(String userRef);

    String getPasswordHash();

    UserCredentials setPasswordHash(String passwordHash);
}
