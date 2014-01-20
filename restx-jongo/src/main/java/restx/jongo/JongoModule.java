package restx.jongo;

import com.mongodb.MongoClient;
import org.jongo.Jongo;
import org.jongo.Mapper;
import org.jongo.marshall.jackson.JacksonMapper;
import restx.factory.Module;
import restx.factory.Name;
import restx.factory.Provides;
import restx.factory.SatisfiedBOM;
import restx.jackson.BsonJodaTimeModule;
import restx.jackson.Views;
import restx.mongo.MongoModule;

import javax.inject.Named;

/**
 * User: xavierhanin
 * Date: 1/19/13
 * Time: 12:12 AM
 */
@Module
public class JongoModule {
    public static final Name<Jongo> NAME = Name.of(Jongo.class, "Jongo");

    @Provides @Named("Mapper")
    public Mapper mapper() {
        return new JacksonMapper.Builder()
                        .registerModule(new BsonJodaTimeModule())
                        .withView(Views.Private.class)
                        .build();
    }

    @Provides @Named("Jongo")
    public Jongo jongo(@Named(MongoModule.MONGO_DB_NAME) String dbName,
                                @Named(MongoModule.MONGO_CLIENT_NAME) MongoClient mongoClient,
                                @Named("Mapper") Mapper mapper) {
        return new Jongo(mongoClient.getDB(dbName), mapper);
    }
}
