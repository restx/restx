package restx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.factory.Factory;
import restx.factory.NamedComponent;
import restx.factory.SingletonFactoryMachine;
import restx.specs.RestxSpecRecorder;
import restx.specs.RestxSpecTape;

import java.io.IOException;

import static restx.StdRestxMainRouter.getMode;

/**
 * User: xavierhanin
 * Date: 2/16/13
 * Time: 3:40 PM
 */
public class RestxMainRouterFactory implements AutoCloseable, RestxMainRouter {
    private final Logger logger = LoggerFactory.getLogger(RestxMainRouterFactory.class);

    private Factory factory;
    private StdRestxMainRouter mainRouter;
    private String contextName = "";
    private RestxSpecRecorder restxSpecRecorder;

    public void setContextName(String contextName) {
        this.contextName = contextName;
    }

    public void init() {
        String baseUri = System.getProperty("restx.baseUri", "");
        if (RestxContext.Modes.RECORDING.equals(getMode())) {
            restxSpecRecorder = new RestxSpecRecorder();
            restxSpecRecorder.install();
        }
        if (getLoadFactoryMode().equals("onstartup")) {
            loadFactory();
            if (!baseUri.isEmpty()) {
                logPrompt(baseUri, "READY");
            } else {
                logger.info("RESTX READY");
            }
        } else {
            if (!baseUri.isEmpty()) {
                logPrompt(baseUri, "LOAD ON REQUEST");
            }
        }
    }

    private void logPrompt(String baseUri, String state) {
        logger.info("\n" +
                "--------------------------------------\n" +
                " -- RESTX " + state + (RestxContext.Modes.RECORDING.equals(getMode()) ? " >> RECORDING MODE <<" : "") + "\n" +
                (mainRouter != null ? (" -- " + mainRouter.getNbFilters() + " filters\n") : "") +
                (mainRouter != null ? (" -- " + mainRouter.getNbRoutes() + " routes\n") : "") +
                " -- for api documentation,\n" +
                " --   VISIT " + baseUri + "/@/api-docs-ui\n" +
                " --\n");
    }

    private void loadFactory() {
        Factory.Builder builder = Factory.builder()
                .addFromServiceLoader()
                .addLocalMachines(Factory.LocalMachines.threadLocal())
                .addLocalMachines(Factory.LocalMachines.contextLocal(contextName));
        if (restxSpecRecorder != null) {
            builder.addLocalMachines(Factory.LocalMachines.contextLocal(RestxContext.Modes.RECORDING));
            builder.addMachine(new SingletonFactoryMachine<>(
                    0, NamedComponent.of(RestxSpecRecorder.class, "specRecorder", restxSpecRecorder)));
        }
        factory = builder.build();

        logger.debug("restx factory ready: {}", factory);

        mainRouter = new StdRestxMainRouter(factory.queryByClass(RestxRouting.class).findOne().get().getComponent());
    }

    private void closeFactory() {
        close();
    }

    private String getLoadFactoryMode() {
        return System.getProperty("restx.factory.load", "onstartup");
    }

    @Override
    public void close() {
        if (factory != null) {
            factory.close();
            factory = null;
            logger.debug("closed restx factory");
        }
    }

    public void route(RestxRequest restxRequest, RestxResponse restxResponse) throws IOException {
        RestxSpecTape tape = null;
        if (restxSpecRecorder != null
                && !restxRequest.getRestxPath().startsWith("/@/")) {
            tape = restxSpecRecorder.record(restxRequest, restxResponse);
            restxRequest = tape.getRecordingRequest();
            restxResponse = tape.getRecordingResponse();
        }

        if (getLoadFactoryMode().equals("onrequest")) {
            loadFactory();
        }

        try {
            mainRouter.route(restxRequest, restxResponse);
        } finally {
            if (tape != null) {
                restxSpecRecorder.stop(tape);
            }
            if (getLoadFactoryMode().equals("onrequest")) {
                closeFactory();
            }
        }
    }

    public static String getFactoryContextName(int port) {
        return String.format("RESTX@%s", port);
    }
}
