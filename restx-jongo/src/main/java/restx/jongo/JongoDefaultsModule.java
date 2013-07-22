package restx.jongo;

import com.google.common.base.Strings;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import restx.factory.Module;
import restx.factory.Provides;

import javax.inject.Named;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * Module providing some sensible defaults for mongo client.
 *
 * Customize by overriding some or all of these components.
 *
 * Eg:
 * -Dmongo.host=dharma.mongohq.com -Dmongo.port=10002 -Dmongo.user=rxinvoice -Dmongo.password=XXXXXXXXX
 */
@Module(priority = 1000)
public class JongoDefaultsModule {

    @Provides @Named("mongo.host")
    public String host() { return "localhost"; }
    @Provides @Named("mongo.port")
    public String port() { return "27017"; }
    @Provides @Named("mongo.user")
    public String user() { return ""; }
    @Provides @Named("mongo.password")
    public String password() { return ""; }

    @Provides
    public MongoClient mongoClient(
            @Named(JongoFactory.JONGO_DB_NAME) String db,
            @Named("mongo.host") String host,
            @Named("mongo.port") String port,
            @Named("mongo.user") String user,
            @Named("mongo.password") String password
    ) {
        int p = Integer.parseInt(port);
        try {
            if (Strings.isNullOrEmpty(user)) {
                return new MongoClient(host, p);
            } else {
                return new MongoClient(
                        new ServerAddress(host, p),
                        Arrays.asList(MongoCredential.createMongoCRCredential(user, db, password.toCharArray())));
            }
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}
