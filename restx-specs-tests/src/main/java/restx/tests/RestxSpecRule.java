package restx.tests;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import restx.factory.Factory;
import restx.server.JettyWebServer;
import restx.server.WebServer;
import restx.server.WebServerSupplier;
import restx.server.WebServers;
import restx.specs.RestxSpec;
import restx.specs.RestxSpecLoader;

import java.io.IOException;
import java.util.Map;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static restx.RestxMainRouterFactory.getFactoryContextName;
import static restx.factory.Factory.LocalMachines;
import static restx.factory.Factory.LocalMachines.contextLocal;

/**
 * User: xavierhanin
 * Date: 3/17/13
 * Time: 1:58 PM
 */
public class RestxSpecRule implements TestRule {
    public static Factory defaultFactory() {
        return Factory.builder()
                .addLocalMachines(LocalMachines.threadLocal())
                .addLocalMachines(contextLocal(RestxSpecRule.class.getSimpleName()))
                .addFromServiceLoader()
                .build();
    }

    public static WebServerSupplier jettyWebServerSupplier(final String webInfLocation, final String appBase) {
        return new WebServerSupplier() {
            @Override
            public WebServer newWebServer(int port) {
                return new JettyWebServer(webInfLocation, appBase, port, "localhost");
            }
        };
    }

    private final WebServerSupplier webServerSupplier;
    private final String routerPath;

    private WebServer server;
    private RestxSpec restxSpec;

    private RestxSpecLoader specLoader = new RestxSpecLoader();

    private Iterable<GivenSpecRule> givenSpecRules;

    /**
     * A shortcut for new RestxSpecRule("/api", jettyWebServerSupplier(webInfLocation, appBase), defaultFactory())
     */
    public RestxSpecRule(String webInfLocation, String appBase) {
        this(webInfLocation, appBase, defaultFactory());
    }

    /**
     * A shortcut for new RestxSpecRule("/api", jettyWebServerSupplier(webInfLocation, appBase), factory)
     */
    public RestxSpecRule(final String webInfLocation, final String appBase, Factory factory) {
        this("/api", jettyWebServerSupplier(webInfLocation, appBase), factory);
    }

    /**
     * Constructs a new RestxSpecRule.
     *
     * @param routerPath the path at which restx router is mounted. eg '/api'
     * @param webServerSupplier a supplier of WebServer, you can use #jettyWebServerSupplier for jetty.
     * @param factory the restx Factory to use to find GivenSpecRuleSupplier s when executing the spec.
     *                This is not used for the server itself.
     */
    public RestxSpecRule(String routerPath, WebServerSupplier webServerSupplier, Factory factory) {
        this.routerPath = routerPath;
        this.webServerSupplier = webServerSupplier;
        givenSpecRules = newArrayList(transform(factory.queryByClass(GivenSpecRuleSupplier.class).findAsComponents(),
                Suppliers.<GivenSpecRule>supplierFunction()));
    }

    protected LocalMachines localMachines() {
        return contextLocal(getFactoryContextName(server.getPort()));
    }

    public void runTest(String spec) throws IOException {
        restxSpec = specLoader.load(spec);
        Map<String, String> params = Maps.newLinkedHashMap();
        for (GivenSpecRule givenSpecRule : givenSpecRules) {
            params.putAll(givenSpecRule.getRunParams());
        }
        params.put(RestxSpec.WhenHttpRequest.CONTEXT_NAME, getFactoryContextName(server.getPort()));
        params.put(RestxSpec.WhenHttpRequest.BASE_URL, server.baseUrl() + routerPath);

        restxSpec.run(ImmutableMap.copyOf(params));
    }

    @Override
    public Statement apply(final Statement statement, Description description) {
        return new Statement() {
                    @Override
                    public void evaluate() throws Throwable {
                        System.out.println("starting server");
                        System.setProperty("restx.factory.load", "onrequest");
                        server = webServerSupplier.newWebServer(WebServers.findAvailablePort());
                        server.start();
                        for (GivenSpecRule givenSpecRule : givenSpecRules) {
                            givenSpecRule.onSetup(localMachines());
                        }

                        System.out.println("server started");
                        try {
                            statement.evaluate();
                        } finally {
                            for (GivenSpecRule givenSpecRule : givenSpecRules) {
                                givenSpecRule.onTearDown(localMachines());
                            }
                            System.out.println("stopping server");
                            server.stop();
                            System.out.println("DONE");
                        }
                    }
                };
    }

}
