package restx.jongo;

import com.google.common.base.Optional;
import restx.annotations.GET;
import restx.annotations.POST;
import restx.annotations.PUT;
import restx.annotations.RestxResource;
import restx.factory.Component;
import restx.security.PermitAll;

import javax.inject.Named;

import static restx.common.MorePreconditions.checkEquals;

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

    @POST("")
    public Optional<String> getNewCredentials(UserAndPass userAndPass) {
        JongoUser user = repository.createUser(new JongoUser(null, userAndPass.getUsername(), "USER"));
        repository.setCredentials(user.getId(), userAndPass.getPassword());
        return repository.findCredentialByUserName(user.getName());
    }

    @PUT("/{username}")
    public Optional<String> updateCredentials(String username, UserAndPass userAndPass) {
        checkEquals("username", username, "userAndPass.username", userAndPass.getUsername());
        JongoUser user = repository.findUserByName(username).get();
        repository.setCredentials(user.getId(), userAndPass.getPassword());
        return repository.findCredentialByUserName(user.getName());
    }

    @GET("")
    public Iterable<UserCredentials> getAllCredentials() {
        return credentials.get().find().as(UserCredentials.class);
    }
}
