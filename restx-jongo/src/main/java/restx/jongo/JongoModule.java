package restx.jongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import org.jongo.Jongo;
import org.jongo.Mapper;
import org.jongo.ObjectIdUpdater;
import org.jongo.marshall.Marshaller;
import org.jongo.marshall.Unmarshaller;
import org.jongo.marshall.jackson.JacksonEngine;
import org.jongo.marshall.jackson.JacksonMapper;
import org.jongo.query.QueryFactory;
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
        final Mapper mapper = new JacksonMapper.Builder()
                .registerModule(new BsonJodaTimeModule())
                .withView(Views.Private.class)
                .build();
        return fixJacksonMapper(mapper);
    }

    public Mapper fixJacksonMapper(Mapper mapper) {
        // the object id updater used by default in jongo does not handle string id properties
        // in a backward compatible way - we are fixing it here
        JacksonEngine jacksonEngine = (JacksonEngine) mapper.getMarshaller();
        ObjectMapper objectMapper = jacksonEngine.getObjectMapper();

        BackwardCompatibleJacksonObjectIdUpdater jacksonObjectIdUpdater
                = new BackwardCompatibleJacksonObjectIdUpdater(objectMapper);
        return new Mapper() {
            @Override
            public Marshaller getMarshaller() {
                return mapper.getMarshaller();
            }

            @Override
            public Unmarshaller getUnmarshaller() {
                return mapper.getUnmarshaller();
            }

            @Override
            public ObjectIdUpdater getObjectIdUpdater() {
                return jacksonObjectIdUpdater;
            }

            @Override
            public QueryFactory getQueryFactory() {
                return mapper.getQueryFactory();
            }
        };
    }

    @Provides @Named("Jongo")
    public Jongo jongo(@Named(MongoModule.MONGO_DB_NAME) String dbName,
                                @Named(MongoModule.MONGO_CLIENT_NAME) MongoClient mongoClient,
                                @Named("Mapper") Mapper mapper) {
        return new Jongo(mongoClient.getDB(dbName), mapper);
    }
}
