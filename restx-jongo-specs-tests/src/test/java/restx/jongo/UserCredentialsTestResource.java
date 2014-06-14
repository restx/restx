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

    private final UserRepositoryById repositoryById;

    private final UserRepositoryByName repositoryByName;

    public UserCredentialsTestResource(@Named("credentials") JongoCollection credentials,
                                       UserRepositoryById repositoryById,
                                       UserRepositoryByName repositoryByName) {
        this.credentials = credentials;
        this.repositoryById = repositoryById;
        this.repositoryByName = repositoryByName;
    }

    @POST("/byId")
    public Optional<String> getNewCredentials(UserAndPass userAndPass) {
        JongoUserById user = repositoryById.createUser(new JongoUserById(null, userAndPass.getUsername(), "USER"));
        repositoryById.setCredentials(user.getId(), userAndPass.getPassword());
        return repositoryById.findCredentialByUserName(user.getName());
    }

    @PUT("/byId/{username}")
    public Optional<String> updateCredentials(String username, UserAndPass userAndPass) {
        checkEquals("username", username, "userAndPass.username", userAndPass.getUsername());
        JongoUserById user = repositoryById.findUserByName(username).get();
        repositoryById.setCredentials(user.getId(), userAndPass.getPassword());
        return repositoryById.findCredentialByUserName(user.getName());
    }

    @POST("/byName")
    public Optional<String> getNewCredentialsFromRepoByName(UserAndPass userAndPass) {
        JongoUserByName user = repositoryByName.createUser(new JongoUserByName(userAndPass.getUsername(), "USER"));
        repositoryByName.setCredentials(user.getName(), userAndPass.getPassword());
        return repositoryByName.findCredentialByUserName(user.getName());
    }

    @PUT("/byName/{username}")
    public Optional<String> updateCredentialsFromRepoByName(String username, UserAndPass userAndPass) {
        checkEquals("username", username, "userAndPass.username", userAndPass.getUsername());
        repositoryByName.setCredentials(username, userAndPass.getPassword());
        return repositoryByName.findCredentialByUserName(username);
    }

    @GET("")
    public Iterable<UserCredentials> getAllCredentials() {
        return credentials.get().find().as(UserCredentials.class);
    }
}
