package restx.tests;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import restx.factory.Factory;
import restx.server.WebServer;
import restx.server.WebServerSupplier;
import restx.server.WebServers;

import static com.google.common.base.Preconditions.checkNotNull;
import static restx.factory.Factory.LocalMachines.contextLocal;

/**
 * User: xavierhanin
 * Date: 5/19/13
 * Time: 5:22 PM
 */
public class RestxServerRule implements TestRule {
    protected final WebServerSupplier webServerSupplier;
    protected WebServer server;
    private String mode = "test";

    /**
     * Default behaviour will look into the @Provided WebServerSupplier class
     */
    public RestxServerRule() {
        this(Factory.getInstance().getComponent(WebServerSupplier.class));
    }

    public RestxServerRule(WebServerSupplier webServerSupplier) {
        this.webServerSupplier = webServerSupplier;
    }

    public HttpTestClient client() {
        return HttpTestClient.withBaseUrl(server.baseUrl());
    }

    public String getMode() {
        return mode;
    }

    public RestxServerRule setMode(final String mode) {
        this.mode = mode;
        return this;
    }

    public void setup() throws Exception {
        System.out.println("starting server");
        server = webServerSupplier.newWebServer(WebServers.findAvailablePort());
        contextLocal(server.getServerId()).set("restx.mode", getMode());
        afterServerCreated();
        server.start();
        afterServerStarted();

        System.out.println("server started");
    }

    public void tearDown() throws Exception {
        beforeServerStop();
        System.out.println("stopping server");
        server.stop();
        afterServerStop();
        System.out.println("DONE");
    }

    @Override
    public Statement apply(final Statement statement, Description description) {
        return new Statement() {
                    @Override
                    public void evaluate() throws Throwable {
                        setup();

                        try {
                            statement.evaluate();
                        } finally {
                            tearDown();
                        }
                    }
                };
    }

    public WebServer getServer() {
        return server;
    }

    protected void afterServerCreated() {
    }

    protected void afterServerStarted() {
    }

    protected void beforeServerStop() {
    }

    protected void afterServerStop() {
    }
}
