package restx;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.classloader.ApplicationClassloader;
import restx.factory.Factory;
import restx.factory.NamedComponent;
import restx.factory.SingletonFactoryMachine;
import restx.specs.RestxSpecRecorder;
import restx.specs.RestxSpecTape;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.Callable;

import static restx.StdRestxMainRouter.getMode;

/**
 * User: xavierhanin
 * Date: 2/16/13
 * Time: 3:40 PM
 */
public class RestxMainRouterFactory {
    private static final Logger logger = LoggerFactory.getLogger(RestxMainRouterFactory.class);

    /**
     * A main router decorator that may record all or some requests.
     *
     * Note that the provided router is only used for non recorded requests.
     * The recorded requests are using a StdRestxMainRouter with a factory initialized per request,
     * to allow recorders to be properly setup in the router factory.
     *
     * Therefore this class is not intended to be exposed outside the main router factory class.
     */
    private static class RecordingMainRouter implements RestxMainRouter {
        private final Optional<RestxSpecRecorder> restxSpecRecorder;
        private final RestxMainRouter router;
        private final String serverId;
        private final Supplier<RestxSpecRecorder> recorderSupplier = new Supplier<RestxSpecRecorder>() {
            @Override
            public RestxSpecRecorder get() {
                RestxSpecRecorder recorder = new RestxSpecRecorder();
                recorder.install();
                return recorder;
            }
        };

        public RecordingMainRouter(String serverId, Optional<RestxSpecRecorder> restxSpecRecorder, RestxMainRouter router) {
            this.serverId = serverId;
            this.restxSpecRecorder = restxSpecRecorder;
            this.router = router;
        }


        @Override
        public void route(final RestxRequest restxRequest, final RestxResponse restxResponse) throws IOException {
            if (!restxRequest.getRestxPath().startsWith("/@/")
                    && (restxSpecRecorder.isPresent() || RestxContext.Modes.RECORDING.equals(getMode(restxRequest)))) {
                logger.debug("RECORDING {}", restxRequest);

                final RestxSpecRecorder restxSpecRecorder = this.restxSpecRecorder.or(recorderSupplier);
                try {
                    RestxSpecRecorder.doWithRecorder(restxSpecRecorder, new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            RestxSpecTape tape = restxSpecRecorder.record(restxRequest, restxResponse);
                            try {
                                // when recording a request we don't use the provided router, we
                                // need to load a new factory, some recorders rely on being available in the factory
                                Factory factory = loadFactory(newFactoryBuilder(serverId,
                                                                                restxSpecRecorder));
                                try {
                                    newStdRouter(factory).route(tape.getRecordingRequest(), tape.getRecordingResponse());
                                } finally {
                                    factory.close();
                                }
                            } finally {
                                RestxSpecRecorder.RecordedSpec recordedSpec = restxSpecRecorder.stop(tape);

                                Optional<String> recordPath = restxRequest.getHeader("RestxRecordPath");
                                if (recordPath.isPresent()) {
                                    // save directly the recorded spec
                                    Optional<String> title = restxRequest.getHeader("RestxRecordTitle");
                                    File recordFile = recordedSpec.getSpec().store(recordPath, title);
                                    logger.info("saved recorded spec in {}", recordFile);
                                }
                            }
                            return null;
                        }
                    });
                } catch (IOException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else if (restxRequest.getRestxPath().startsWith("/@/") && restxSpecRecorder.isPresent()) {
                // set current recorder for admin routes, some may be using it
                // to provide information on the recorder itself
                try {
                    RestxSpecRecorder.doWithRecorder(restxSpecRecorder.get(), new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            router.route(restxRequest, restxResponse);
                            return null;
                        }
                    });
                } catch (IOException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                router.route(restxRequest, restxResponse);
            }
        }
    }

    public static RestxMainRouter newInstance(final String serverId, String baseUri) {
        final ApplicationClassloader applicationClassloader = new ApplicationClassloader(new File("."), "src/main/java", "src/main/resources", "target/generated-sources/annotations");
        logger.info("LOADING MAIN ROUTER");
        Optional<RestxSpecRecorder> recorder;
        if (RestxContext.Modes.RECORDING.equals(getMode())) {
            recorder = Optional.of(new RestxSpecRecorder());
            recorder.get().install();
        } else {
            recorder = Optional.absent();
        }

        if (getLoadFactoryMode().equals("onstartup")) {
            final StdRestxMainRouter mainRouter = newStdRouter(loadFactory(newFactoryBuilder(serverId)));
            logPrompt(baseUri, "READY", mainRouter);

            if (RestxContext.Modes.PROD.equals(getMode())) {
                // in PROD we definitely return the main router, we will never check anything else
                return mainRouter;
            } else {
                // in other modes we may record requests one by one or all of them, we use the recording decorator
                return new RecordingMainRouter(serverId, recorder, mainRouter);
            }
        } else if (getLoadFactoryMode().equals("onrequest")) {
            logPrompt(baseUri, ">> LOAD ON REQUEST <<", null);

            return new RecordingMainRouter(serverId, recorder, new RestxMainRouter() {
                @Override
                public void route(RestxRequest restxRequest, RestxResponse restxResponse) throws IOException {
                    ClassLoader previousLoader =
                                        Thread.currentThread().getContextClassLoader();

                    Thread.currentThread().setContextClassLoader(applicationClassloader);
                    applicationClassloader.detectChanges();

                    try {
                        Factory factory = loadFactory(newFactoryBuilder(serverId,
                                                                        RestxSpecRecorder.current().orNull()));
                        try {
                            newStdRouter(factory).route(restxRequest, restxResponse);
                        } finally {
                            factory.close();
                        }
                    } finally {
                        Thread.currentThread().setContextClassLoader(previousLoader);
                    }
                }
            });
        } else {
            throw new IllegalStateException("illegal load factory mode: '" + getLoadFactoryMode() + "'. " +
                    "It must be either 'onstartup' or 'onrequest'.");
        }
    }

    private static void logPrompt(String baseUri, String state, StdRestxMainRouter mainRouter) {
        logger.info("\n" +
                "--------------------------------------\n" +
                " -- RESTX " + state + " >> " + getMode().toUpperCase(Locale.ENGLISH)+ " MODE <<\n" +
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
        Factory.Builder builder = newFactoryBuilder();
        if (contextName != null) {
            builder.addLocalMachines(Factory.LocalMachines.contextLocal(contextName));
        }
        return builder;
    }

    private static Factory.Builder newFactoryBuilder() {
        return Factory.builder()
                    .addFromServiceLoader()
                    .addMachine(new SingletonFactoryMachine<>(0, NamedComponent.of(String.class, "restx.mode", getMode())))
                    .addLocalMachines(Factory.LocalMachines.threadLocal());
    }

    private static StdRestxMainRouter newStdRouter(Factory factory) {
        return new StdRestxMainRouter(factory.queryByClass(RestxRouting.class).findOne().get().getComponent());
    }

    private static String getLoadFactoryMode() {
        return System.getProperty("restx.factory.load", RestxContext.Modes.RECORDING.equals(getMode())
                ? "onrequest" : "onstartup");
    }

    private RestxMainRouterFactory() {}
}
