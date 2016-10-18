package restx.jongo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import org.bson.types.ObjectId;
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
@RestxResource("/api")
@PermitAll
public class UserCredentialsTestResource {
    public static class Pojo {
        @JsonProperty("_id")
        private final String id;

        public Pojo(@JsonProperty("_id") String id) {
            this.id = id;
        }
    }

    private final JongoCollection users;

    private final JongoCollection credentials;

    public UserCredentialsTestResource(@Named("users") JongoCollection users,
                                       @Named("credentials") JongoCollection credentials) {
        this.users = users;
        this.credentials = credentials;
    }

    @GET("/newcredentials")
    public UserCredentials getNewCredentials() {
        String id = ObjectId.get().toString();
        UserCredentials userCredentials = new UserCredentials().setUserRef(id);
        credentials.get().save(userCredentials);
        return userCredentials;
    }
}
