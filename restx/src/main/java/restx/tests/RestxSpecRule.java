package restx.tests;

import com.google.common.collect.ImmutableMap;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.joda.time.DateTime;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import restx.factory.Factory;
import restx.factory.NamedComponent;
import restx.factory.SingletonFactoryMachine;
import restx.jongo.JongoFactory;
import restx.server.WebServer;
import restx.server.WebServers;
import restx.servlet.RestxMainRouterServlet;
import restx.specs.RestxSpec;

import java.io.IOException;

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
        RestxSpec.load(spec)
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
}
