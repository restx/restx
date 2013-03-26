package restx.tests;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.UnmodifiableIterator;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.jongo.Mapper;
import org.jongo.MongoCollection;
import org.jongo.ObjectIdUpdater;
import org.jongo.marshall.Marshaller;
import org.jongo.marshall.Unmarshaller;
import org.jongo.query.QueryFactory;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import restx.factory.*;
import restx.jongo.JongoCollection;
import restx.jongo.JongoFactory;
import restx.jongo.StdJongoCollection;
import restx.server.WebServer;
import restx.server.WebServers;
import restx.servlet.RestxMainRouterServlet;
import restx.specs.RestxSpec;

import java.io.IOException;
import java.util.Map;

/**
 * User: xavierhanin
 * Date: 3/17/13
 * Time: 1:58 PM
 */
public class RestxSpecRule implements TestRule {
    private WebServer server;
    private final String db;

    private final String webInfLocation;
    private final String appBase;

    private RestxSpec restxSpec;
    private Map<String, CollectionSequence> sequences = Maps.newLinkedHashMap();

    public RestxSpecRule(String db, String webInfLocation, String appBase) {
        this.db =  db + "-test-" + DateTime.now().getMillis();;
        this.webInfLocation = webInfLocation;
        this.appBase = appBase;
    }

    protected Factory.LocalMachines localMachines() {
        return restx.factory.Factory.LocalMachines
                .contextLocal(RestxMainRouterServlet.getFactoryContextName(server.getPort()));
    }

    public void runTest(String spec) throws IOException {
        restxSpec = RestxSpec.load(spec);
        restxSpec
                .run(ImmutableMap.of(
                        RestxSpec.GivenCollection.DB_URI, "mongodb://localhost/" + db,
                        RestxSpec.WhenHttpRequest.BASE_URL, server.baseUrl() + "/api"));
    }

    @Override
    public Statement apply(final Statement statement, Description description) {
        return new Statement() {
                    @Override
                    public void evaluate() throws Throwable {
                        System.out.println("starting server");
                        System.setProperty("restx.factory.load", "onrequest");
                        server = WebServers.newWebServer(webInfLocation, appBase, WebServers.findAvailablePort());
                        server.start();

                        localMachines()
                                .addMachine(new SingletonFactoryMachine<>(
                                        -10, new NamedComponent<>(JongoFactory.JONGO_DB, db)));

                        final Factory.Query<Mapper> mapperQuery = Factory.Query.byClass(Mapper.class);
                        localMachines().addMachine(
                                FactoryMachineWrapper.from(new StdJongoCollection.JongoCollectionFactory())
                                        .withPriority(-10)
                                        .withDependencies(mapperQuery)
                                        .transformComponents(new Function<Map.Entry<SatisfiedBOM, NamedComponent>, NamedComponent>() {
                                            @Override
                                            public NamedComponent apply(final Map.Entry<SatisfiedBOM, NamedComponent> input) {
                                                final JongoCollection collection = (JongoCollection) input.getValue().getComponent();
                                                final Mapper mapper = input.getKey().getOne(mapperQuery).get().getComponent();
                                                final ObjectIdUpdater objectIdUpdater = mapper.getObjectIdUpdater();

                                                return new NamedComponent<>(input.getValue().getName(),
                                                        new JongoCollection() {
                                                            @Override
                                                            public String getName() {
                                                                return collection.getName();
                                                            }

                                                            @Override
                                                            public MongoCollection get() {
                                                                MongoCollection mongoCollection = collection.get();
                                                                mongoCollection = new MongoCollection(
                                                                        mongoCollection.getDBCollection(),
                                                                        new Mapper() {
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
                                                                                return new ObjectIdUpdater() {
                                                                                    @Override
                                                                                    public boolean canSetObjectId(Object target) {
                                                                                        return objectIdUpdater.canSetObjectId(target);
                                                                                    }

                                                                                    @Override
                                                                                    public void setDocumentGeneratedId(Object target, ObjectId id) {
                                                                                        objectIdUpdater.setDocumentGeneratedId(target,
                                                                                                new ObjectId(getGivenCollectionSequence(collection.getName())
                                                                                                        .next().or(id.toString())));
                                                                                    }
                                                                                };
                                                                            }

                                                                            @Override
                                                                            public QueryFactory getQueryFactory() {
                                                                                return mapper.getQueryFactory();
                                                                            }
                                                                        });
                                                                return mongoCollection;
                                                            }
                                                        });
                                            }
                                        }).build());


                        System.out.println("server started");
                        try {
                            statement.evaluate();
                        } finally {
                            System.out.println("dropping database " + db);
                            new MongoClient(new MongoClientURI("mongodb://localhost")).dropDatabase(db);
                            System.out.println("stopping server");
                            server.stop();
                            System.out.println("DONE");
                        }
                    }
                };
    }

    private CollectionSequence getGivenCollectionSequence(String name) {
        CollectionSequence sequence = sequences.get(name);
        if (sequence == null) {
            for (RestxSpec.Given given : restxSpec.getGiven()) {
                if (given instanceof RestxSpec.GivenCollection) {
                    RestxSpec.GivenCollection collection = (RestxSpec.GivenCollection) given;
                    if (collection.getCollection().equals(name)) {
                        final UnmodifiableIterator<String> it = collection.getSequence().iterator();
                        sequence = new CollectionSequence() {
                            @Override
                            public Optional<String> next() {
                                return it.hasNext() ? Optional.of(it.next()) : Optional.<String>absent();
                            }
                        };
                        sequences.put(name, sequence);
                        break;
                    }
                }
            }
        }
        return sequence;
    }

    public static interface CollectionSequence {
        Optional<String> next();
    }
}
