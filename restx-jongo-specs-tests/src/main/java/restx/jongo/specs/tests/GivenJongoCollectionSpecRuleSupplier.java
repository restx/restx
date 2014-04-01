package restx.jongo.specs.tests;

import com.google.common.collect.ImmutableMap;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.joda.time.DateTime;
import restx.factory.*;
import restx.mongo.MongoModule;
import restx.mongo.MongoSettings;
import restx.specs.mongo.GivenJongoCollection;
import restx.tests.GivenSpecRule;
import restx.tests.GivenSpecRuleSupplier;

import javax.inject.Inject;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * User: xavierhanin
 * Date: 3/31/13
 * Time: 8:25 AM
 */
@Component
public class GivenJongoCollectionSpecRuleSupplier implements GivenSpecRuleSupplier {
    private String db;
    private String uri;

    @Inject
    public GivenJongoCollectionSpecRuleSupplier(MongoSettings mongoSettings) {
        this.db = mongoSettings.dbName();
        this.uri = mongoSettings.uri();
    }

    @Override
    public GivenSpecRule get() {
        return new GivenJongoCollectionSpecRule(db, uri);
    }

    public static class GivenJongoCollectionSpecRule implements GivenSpecRule {
        private static final AtomicLong counter = new AtomicLong();
        private final String db;
        private final String uri;

        public GivenJongoCollectionSpecRule(String db, String uri) {
            this.db = db + "-test-" + DateTime.now().getMillis() + "-" + counter.incrementAndGet();
            this.uri = uri;
        }

        @Override
        public Map<String, String> getRunParams() {
            return ImmutableMap.of(GivenJongoCollection.DB_URI, uri + "/" + db);
        }

        @Override
        public void onSetup(Factory.LocalMachines localMachines) {
            System.out.println("using db " + db);
            localMachines
                    .addMachine(new SingletonFactoryMachine<>(
                            -10, new NamedComponent<>(Name.of(String.class, MongoModule.MONGO_DB_NAME), db)));
        }

        @Override
        public void onTearDown(Factory.LocalMachines localMachines) {
            System.out.println("dropping database " + db);
            try {
                new MongoClient(new MongoClientURI(uri)).dropDatabase(db);
            } catch (UnknownHostException e) {
                throw new IllegalStateException("got unknown host exception while contacting mongo db on localhost", e);
            }
        }
    }
}
