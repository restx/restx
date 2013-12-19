package restx.security;

import org.mindrot.jbcrypt.BCrypt;

/**
 * A BCrypt based credentials checker.
 *
 * This is the most recommended credentials checker at the time being.
 */
public class BCryptCredentialsStrategy implements CredentialsStrategy {
    @Override
    public boolean checkCredentials(String userName, String providedPasswordHash, String storedCredentials) {
        return BCrypt.checkpw(providedPasswordHash, storedCredentials);
    }

    @Override
    public String cryptCredentialsForStorage(String userName, String providedPasswordHash) {
        return BCrypt.hashpw(providedPasswordHash, BCrypt.gensalt());
    }
}
