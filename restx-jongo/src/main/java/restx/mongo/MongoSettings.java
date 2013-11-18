package restx.mongo;

import restx.config.Settings;
import restx.config.SettingsKey;

/**
* Date: 18/11/13
* Time: 21:36
*/
@Settings
public interface MongoSettings {
    @SettingsKey(key = MongoModule.MONGO_DB_NAME, doc = "the name of the mongo database to use")
    String dbName();

    @SettingsKey(key = "mongo.uri", defaultValue = "mongodb://localhost:27017",
        doc = "the mongo URI to use to connect to mongodb. " +
                "See http://api.mongodb.org/java/current/com/mongodb/MongoClientURI.html")
    String uri();
}
