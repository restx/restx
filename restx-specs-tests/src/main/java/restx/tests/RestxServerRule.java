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
    public static Factory defaultFactory() {
        return RestxSpecRunner.defaultFactory();
    }

    protected final WebServerSupplier webServerSupplier;
    protected WebServer server;
    private String factoryLoadMode = "onrequest";

    /**
     * Default behaviour will look into the @Provided WebServerSupplier class
     */
    public RestxServerRule(){
        this(defaultFactory().queryByClass(WebServerSupplier.class).findOne().get().getComponent());
    }

    public RestxServerRule(WebServerSupplier webServerSupplier) {
        this.webServerSupplier = webServerSupplier;
    }

    protected Factory.LocalMachines localMachines() {
        return contextLocal(checkNotNull(server, "server has not been created yet").getServerId());
    }

    public String getFactoryLoadMode() {
        return factoryLoadMode;
    }

    public RestxServerRule setFactoryLoadMode(String factoryLoadMode) {
        this.factoryLoadMode = factoryLoadMode;
        return this;
    }

    @Override
    public Statement apply(final Statement statement, Description description) {
        return new Statement() {
                    @Override
                    public void evaluate() throws Throwable {
                        System.out.println("starting server");
                        System.setProperty("restx.factory.load", factoryLoadMode);
                        server = webServerSupplier.newWebServer(WebServers.findAvailablePort());
                        afterServerCreated();
                        server.start();
                        afterServerStarted();

                        System.out.println("server started");
                        try {
                            statement.evaluate();
                        } finally {
                            beforeServerStop();
                            System.out.println("stopping server");
                            server.stop();
                            afterServerStop();
                            System.out.println("DONE");
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
