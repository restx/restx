package restx.security;

/**
 * Date: 14/12/13
 * Time: 15:05
 */
public interface CredentialsStrategy {
    boolean checkCredentials(String userName, String providedPasswordHash, String storedCredentials);
    String cryptCredentialsForStorage(String userName, String providedPasswordHash);
}
