package restx.tests;

import restx.factory.Factory;
import restx.server.WebServerSupplier;
import restx.specs.RestxSpec;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;
import static restx.tests.ServerSuppliers.jettyWebServerSupplier;

/**
 * User: xavierhanin
 * Date: 3/17/13
 * Time: 1:58 PM
 */
public class RestxSpecRule extends RestxServerRule {
    public static Factory defaultFactory() {
        return RestxSpecRunner.defaultFactory();
    }

    private final String routerPath;
    private final Factory factory;

    private RestxSpecRunner runner;


    /**
     * A shortcut for new RestxSpecRule("src/main/webapp/WEB-INF/web.xml", "src/main/webapp",
     *                          "/api", jettyWebServerSupplier(webInfLocation, appBase), defaultFactory())
     */
    public RestxSpecRule() {
        this("src/main/webapp/WEB-INF/web.xml", "src/main/webapp");
    }

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
        super(webServerSupplier);
        this.routerPath = routerPath;
        this.factory = factory;
    }

    public void runTest(String spec) throws IOException {
        getRunner().runTest(spec);
    }

    public RestxSpec loadSpec(String spec) throws IOException {
        return getRunner().loadSpec(spec);
    }

    public void runTest(RestxSpec restxSpec) {
        getRunner().runTest(restxSpec);
    }

    private RestxSpecRunner getRunner() {
        return checkNotNull(runner, "server not started");
    }

    @Override
    protected void afterServerCreated() {
        runner = new RestxSpecRunner(routerPath, server.getServerId(), server.baseUrl(), factory);
    }

    @Override
    protected void beforeServerStop() {
        runner.dispose();
        runner = null;
    }
}
