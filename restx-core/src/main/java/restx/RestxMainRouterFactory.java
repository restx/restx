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
public class RestxMainRouterFactory {
    private static final Logger logger = LoggerFactory.getLogger(RestxMainRouterFactory.class);

    public static RestxMainRouter newInstance(String baseUri) {
        logger.info("LOADING MAIN ROUTER");
        if (getLoadFactoryMode().equals("onstartup")) {
            StdRestxMainRouter mainRouter = newStdRouter(loadFactory(newFactoryBuilder()));
            logPrompt(baseUri, "READY", mainRouter);

            return mainRouter;
        } else if (getLoadFactoryMode().equals("onrequest")) {
            final RestxSpecRecorder restxSpecRecorder = RestxContext.Modes.RECORDING.equals(getMode()) ?
                    new RestxSpecRecorder() : null;
            if (restxSpecRecorder != null) {
                restxSpecRecorder.install();
            }

            logPrompt(baseUri, ">> LOAD ON REQUEST <<", null);

            return new RestxMainRouter() {
                @Override
                public void route(RestxRequest restxRequest, RestxResponse restxResponse) throws IOException {
                    RestxSpecTape tape = null;
                    if (restxSpecRecorder != null
                            && !restxRequest.getRestxPath().startsWith("/@/")) {
                        tape = restxSpecRecorder.record(restxRequest, restxResponse);
                        restxRequest = tape.getRecordingRequest();
                        restxResponse = tape.getRecordingResponse();
                    }

                    Factory factory = loadFactory(newFactoryBuilder(getFactoryContextName(restxRequest.getPort()),
                                                                    restxSpecRecorder));

                    try {
                        newStdRouter(factory).route(restxRequest, restxResponse);
                    } finally {
                        if (tape != null) {
                            restxSpecRecorder.stop(tape);
                        }
                        factory.close();
                    }
                }
            };
        } else {
            throw new IllegalStateException("illegal load factory mode: '" + getLoadFactoryMode() + "'. " +
                    "It must be either 'onstartup' or 'onrequest'.");
        }
    }

    private static void logPrompt(String baseUri, String state, StdRestxMainRouter mainRouter) {
        logger.info("\n" +
                "--------------------------------------\n" +
                " -- RESTX " + state + (RestxContext.Modes.RECORDING.equals(getMode()) ? " >> RECORDING MODE <<" : "") + "\n" +
                (mainRouter != null ? (" -- " + mainRouter.getNbFilters() + " filters\n") : "") +
                (mainRouter != null ? (" -- " + mainRouter.getNbRoutes() + " routes\n") : "") +
                (baseUri == null || baseUri.isEmpty() ? "" :
                        " -- for admin console,\n" +
                        " --   VISIT " + baseUri + "/@/ui/\n") +
                " --\n");
    }

    private static Factory loadFactory(Factory.Builder builder) {
        Factory factory = builder.build();
        logger.debug("restx factory ready: {}", factory.dumper());

        return factory;
    }

    private static Factory.Builder newFactoryBuilder(String contextName, RestxSpecRecorder specRecorder) {
        Factory.Builder builder = newFactoryBuilder(contextName);
        if (specRecorder != null) {
            builder
                .addLocalMachines(Factory.LocalMachines.contextLocal(RestxContext.Modes.RECORDING))
                .addMachine(new SingletonFactoryMachine<>(
                        0, NamedComponent.of(RestxSpecRecorder.class, "specRecorder", specRecorder)));
        }
        return builder;
    }

    private static Factory.Builder newFactoryBuilder(String contextName) {
        return newFactoryBuilder()
                    .addLocalMachines(Factory.LocalMachines.contextLocal(contextName));
    }

    private static Factory.Builder newFactoryBuilder() {
        return Factory.builder()
                    .addFromServiceLoader()
                    .addLocalMachines(Factory.LocalMachines.threadLocal());
    }

    private static StdRestxMainRouter newStdRouter(Factory factory) {
        return new StdRestxMainRouter(factory.queryByClass(RestxRouting.class).findOne().get().getComponent());
    }

    private static String getLoadFactoryMode() {
        return System.getProperty("restx.factory.load", RestxContext.Modes.RECORDING.equals(getMode())
                ? "onrequest" : "onstartup");
    }

    public static String getFactoryContextName(int port) {
        return String.format("RESTX@%s", port);
    }

    private RestxMainRouterFactory() {}
}
