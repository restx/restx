package restx;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.factory.Factory;
import restx.specs.SpecRecorder;

import java.io.IOException;

/**
 * User: xavierhanin
 * Date: 2/16/13
 * Time: 3:40 PM
 */
public class RestxMainRouterFactory implements AutoCloseable, RestxMainRouter {
    private static final String PROD = "prod";
    private static final String DEV = "dev";
    private static final String RECORDING = SpecRecorder.RECORDING;

    private final Logger logger = LoggerFactory.getLogger(RestxMainRouterFactory.class);

    private Factory factory;
    private StdRestxMainRouter mainRouter;
    private String contextName = "";

    public void setContextName(String contextName) {
        this.contextName = contextName;
    }

    public void init() {
        String baseUri = System.getProperty("restx.baseUri", "");
        if (RECORDING.equals(getMode())) {
            SpecRecorder.install();
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
                " -- RESTX " + state + (RECORDING.equals(getMode()) ? " >> RECORDING MODE <<" : "") + "\n" +
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
        if (RECORDING.equals(getMode())) {
            builder.addLocalMachines(Factory.LocalMachines.contextLocal(RECORDING));
        }
        factory = builder.build();

        logger.debug("restx factory ready: {}", factory);

        mainRouter = new StdRestxMainRouter(ImmutableList.copyOf(
                factory.queryByClass(RestxRoute.class).findAsComponents()));
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
        if (RECORDING.equals(getMode())) {
            SpecRecorder recorder = SpecRecorder.record(restxRequest, restxResponse);
            restxRequest = recorder.getRecordingRequest();
            restxResponse = recorder.getRecordingResponse();
        }

        if (getLoadFactoryMode().equals("onrequest")) {
            loadFactory();
        }

        try {
            mainRouter.route(restxRequest, restxResponse);
        } finally {
            if (getLoadFactoryMode().equals("onrequest")) {
                closeFactory();
            }
        }
    }

    private String getMode() {
        return System.getProperty("restx.mode", PROD);
    }
}
