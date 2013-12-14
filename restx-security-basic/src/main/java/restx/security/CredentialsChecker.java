package restx.security;

/**
 * Date: 14/12/13
 * Time: 15:05
 */
public interface CredentialsChecker {
    boolean checkCredentials(String userName, String providedPasswordHash, String storedCredentials);
}
