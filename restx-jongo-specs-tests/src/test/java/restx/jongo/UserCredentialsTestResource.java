package restx.jongo;

import com.google.common.base.Optional;
import restx.annotations.GET;
import restx.annotations.RestxResource;
import restx.factory.Component;
import restx.security.PermitAll;

import javax.inject.Named;

/**
 * User: Christophe Labouisse
 * Date: 11/06/2014
 * Time: 16:21
 */
@Component
@RestxResource("/api/credentials")
@PermitAll
public class UserCredentialsTestResource {
    private final JongoCollection credentials;

    private final MyUserRepository repository;

    public UserCredentialsTestResource(@Named("credentials") JongoCollection credentials, MyUserRepository repository) {
        this.credentials = credentials;
        this.repository = repository;
    }

    @GET("/new")
    public Optional<String> getNewCredentials() {
        JongoUser user = repository.createUser(new JongoUser(null, "user", "USER"));
        repository.setCredentials(user.getId(), "bad password");
        return repository.findCredentialByUserName(user.getName());
    }

    @GET("/{username}")
    public Optional<String> updateCredentials(String username) {
        JongoUser user = repository.findUserByName(username).get();
        repository.setCredentials(user.getId(), "new password");
        return repository.findCredentialByUserName(user.getName());
    }

    @GET("")
    public Iterable<UserCredentials> getAllCredentials() {
        return credentials.get().find().as(UserCredentials.class);
    }
}
