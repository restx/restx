package restx.security;

/**
 * A very basic checker which compares stored credentials with provided one.
 *
 * Note that this mean that credentials are probably stored without salt and with very basic hashing which is not a
 * good practice. But in some situations where app has very limited access it may be ok.
 */
public class EqualsCredentialsChecker implements CredentialsChecker {
    @Override
    public boolean checkCredentials(String userName, String providedPasswordHash, String storedCredentials) {
        return providedPasswordHash.equals(storedCredentials);
    }
}
