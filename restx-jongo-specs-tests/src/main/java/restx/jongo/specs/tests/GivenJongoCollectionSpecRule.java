package restx.jongo.specs.tests;

import com.google.common.collect.ImmutableMap;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import restx.factory.Factory;
import restx.factory.NamedComponent;
import restx.factory.SingletonFactoryMachine;
import restx.jongo.JongoFactory;
import restx.specs.mongo.GivenJongoCollection;
import restx.tests.GivenSpecRule;

import java.net.UnknownHostException;
import java.util.Map;

/**
 * User: xavierhanin
 * Date: 3/30/13
 * Time: 11:56 PM
 */
public class GivenJongoCollectionSpecRule implements GivenSpecRule {
    private final String db;

    public GivenJongoCollectionSpecRule(String db) {
        this.db = db;
    }

    @Override
    public Map<String, String> getRunParams() {
        return ImmutableMap.of(GivenJongoCollection.DB_URI, "mongodb://localhost/" + db);
    }

    @Override
    public void onSetup(Factory.LocalMachines localMachines) {
        localMachines
                .addMachine(new SingletonFactoryMachine<>(
                        -10, new NamedComponent<>(JongoFactory.JONGO_DB, db)));
    }

    @Override
    public void onTearDown(Factory.LocalMachines localMachines) {
        System.out.println("dropping database " + db);
        try {
            new MongoClient(new MongoClientURI("mongodb://localhost")).dropDatabase(db);
        } catch (UnknownHostException e) {
            throw new IllegalStateException("got unknown host exception while contacting mongo db on localhost", e);
        }
    }
}
