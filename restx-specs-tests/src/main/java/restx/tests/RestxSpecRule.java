package restx.tests;

import restx.factory.Factory;
import restx.server.WebServerSupplier;
import restx.specs.RestxSpec;
import restx.specs.RestxSpecLoader;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: xavierhanin
 * Date: 3/17/13
 * Time: 1:58 PM
 */
public class RestxSpecRule extends RestxServerRule {
    private final String routerPath;
    private final Factory factory;

    private RestxSpecRunner runner;

    /**
     * A shortcut for new RestxSpecRule("/api", queryByClass(WebServerSupplier.class), Factory.getInstance())
     */
    public RestxSpecRule() {
        this(Factory.getInstance().queryByClass(WebServerSupplier.class).findOne().get().getComponent());
    }

    /**
     * A shortcut for new RestxSpecRule(routerPath, queryByClass(WebServerSupplier.class), Factory.getInstance())
     */
    public RestxSpecRule(String routerPath) {
        this(routerPath, Factory.getInstance().queryByClass(WebServerSupplier.class).findOne().get().getComponent(), Factory.getInstance());
    }

    /**
     * A shortcut for new RestxSpecRule("/api", webServerSupplier, Factory.getInstance())
     */
    public RestxSpecRule(WebServerSupplier webServerSupplier) {
        this("/api", webServerSupplier, Factory.getInstance());
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
        runner = new RestxSpecRunner(
                new RestxSpecLoader(factory), routerPath, server.getServerId(), server.baseUrl(), factory);
    }

    @Override
    protected void beforeServerStop() {
        runner.dispose();
        runner = null;
    }
}
