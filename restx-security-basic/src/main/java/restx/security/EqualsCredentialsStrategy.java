package restx.security;

/**
 * A very basic credentials strategy with no encryption at all.
 *
 * Note that this mean that credentials are probably stored without salt and with very basic client side hashing
 * which is not a good practice. But in some situations where app has very limited access it may be ok.
 */
public class EqualsCredentialsStrategy implements CredentialsStrategy {
    @Override
    public boolean checkCredentials(String userName, String providedPasswordHash, String storedCredentials) {
        return providedPasswordHash.equals(storedCredentials);
    }

    @Override
    public String cryptCredentialsForStorage(String userName, String providedPasswordHash) {
        return providedPasswordHash;
    }
}
