package restx;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.net.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.classloader.CompilationFinishedEvent;
import restx.classloader.CompilationManager;
import restx.classloader.HotReloadingClassLoader;
import restx.factory.Factory;
import restx.factory.NamedComponent;
import restx.factory.SingletonFactoryMachine;
import restx.server.WebServers;
import restx.specs.RestxSpec;
import restx.specs.RestxSpecRecorder;
import restx.specs.RestxSpecTape;

import javax.tools.Diagnostic;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
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
        private final EventBus eventBus;
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

        public RecordingMainRouter(String serverId, EventBus eventBus, Optional<RestxSpecRecorder> restxSpecRecorder, RestxMainRouter router) {
            this.serverId = serverId;
            this.eventBus = eventBus;
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
                                Factory factory = loadFactory(newFactoryBuilder(serverId, eventBus,
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
                                    String title = restxRequest.getHeader("RestxRecordTitle")
                                                                .or(recordedSpec.getSpec().getTitle());
                                    File recordFile = recordedSpec.getSpec()
                                            .withTitle(title)
                                            .withPath(RestxSpec.buildPath(recordPath, title))
                                            .store();
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

    private static class PerRequestFactoryLoader implements RestxMainRouter {
        private final String serverId;
        private final EventBus eventBus;

        public PerRequestFactoryLoader(String serverId, EventBus eventBus) {
            this.serverId = serverId;
            this.eventBus = eventBus;
        }

        @Override
        public void route(RestxRequest restxRequest, RestxResponse restxResponse) throws IOException {
            Factory factory = loadFactory(newFactoryBuilder(serverId, eventBus,
                    RestxSpecRecorder.current().orNull()));
            try {
                newStdRouter(factory).route(restxRequest, restxResponse);
            } finally {
                factory.close();
            }
        }
    }

    /**
     * Enables hot reload: this rely on classes compiled by an external compiler
     */
    private static class HotReloadRouter implements RestxMainRouter {
        private final RestxMainRouter delegate;
        private final String rootPackage;

        public HotReloadRouter(RestxMainRouter delegate) {
            this.delegate = delegate;
            this.rootPackage = System.getProperty("restx.app.package");
        }

        @Override
        public void route(RestxRequest restxRequest, RestxResponse restxResponse) throws IOException {
            ClassLoader previousLoader =
                    Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(new HotReloadingClassLoader(
                        previousLoader, rootPackage));
                delegate.route(restxRequest, restxResponse);
            } finally {
                Thread.currentThread().setContextClassLoader(previousLoader);
            }
        }
    }

    private static class CompilationManagerRouter implements RestxMainRouter {
        private final RestxMainRouter delegate;
        private final String rootPackage;
        private final Path destinationDir;
        private final CompilationManager compilationManager;
        private HotReloadingClassLoader hotReloadingClassLoader;

        public CompilationManagerRouter(RestxMainRouter delegate, EventBus eventBus) {
            this.delegate = delegate;
            this.rootPackage = System.getProperty("restx.app.package");
            compilationManager = Apps.newAppCompilationManager(eventBus);
            destinationDir = compilationManager.getDestination();
            eventBus.register(new Object() {
                @Subscribe
                public void onCompilationFinished(
                        CompilationFinishedEvent event) {
                    setClassLoader();
                }
            });
            setClassLoader();
            compilationManager.incrementalCompile();
            if (useAutoCompile()) {
                compilationManager.startAutoCompile();
            }
        }

        private void setClassLoader() {
            hotReloadingClassLoader = new HotReloadingClassLoader(
                    Thread.currentThread().getContextClassLoader(), rootPackage) {
                protected InputStream getInputStream(String path) {
                    try {
                        return Files.newInputStream(destinationDir.resolve(path));
                    } catch (IOException e) {
                        return null;
                    }
                }
            };
        }

        @Override
        public void route(RestxRequest restxRequest, RestxResponse restxResponse) throws IOException {
            ClassLoader previousLoader =
                    Thread.currentThread().getContextClassLoader();
            try {
                if (!useAutoCompile()) {
                    compilationManager.incrementalCompile();
                }

                Collection<Diagnostic<?>> lastDiagnostics = compilationManager.getLastDiagnostics();
                if (!lastDiagnostics.isEmpty()) {
                    restxResponse.setStatus(HttpStatus.SERVICE_UNAVAILABLE.getCode());
                    restxResponse.setContentType(MediaType.PLAIN_TEXT_UTF_8.toString());
                    PrintWriter restxResponseWriter = restxResponse.getWriter();
                    restxResponseWriter.write("COMPILATION ERROR(S):\n\n\n");
                    for (Diagnostic<?> d : lastDiagnostics) {
                        if (d.getKind() != Diagnostic.Kind.NOTE) {
                            restxResponseWriter.write(d + "\n\n");
                        }
                    }
                    return;
                }

                Thread.currentThread().setContextClassLoader(hotReloadingClassLoader);
                delegate.route(restxRequest, restxResponse);
            } finally {
                Thread.currentThread().setContextClassLoader(previousLoader);
            }
        }
    }

    public static RestxMainRouter newInstance(final String serverId, String baseUri) {
        logger.info("LOADING MAIN ROUTER");
        if (RestxContext.Modes.DEV.equals(getMode()) && !useHotCompile()) {
            logger.info("\nHot compile is not enabled, no on the fly compilation will be performed\n" +
                    "To enable it, use '-Drestx.app.package=<rootAppPackage> -Drestx.router.hotcompile=true' as VM argument\n" +
                    "and make sure you have JDK tools.jar in your classpath");
            if (!useHotReload()) {
                logger.info("\nHot reload is not enabled either, no hot reload on recompilation will be performed\n" +
                        "To enable it, use '-Drestx.app.package=<rootAppPackage> -Drestx.router.hotreload=true' as VM argument");
            }
        }

        EventBus eventBus = WebServers.getServerById(serverId).get().getEventBus();

        Optional<RestxSpecRecorder> recorder;
        if (RestxContext.Modes.RECORDING.equals(getMode())) {
            recorder = Optional.of(new RestxSpecRecorder());
            recorder.get().install();
        } else {
            recorder = Optional.absent();
        }

        if (getLoadFactoryMode().equals("onstartup")) {
            final StdRestxMainRouter mainRouter = newStdRouter(loadFactory(newFactoryBuilder(serverId, eventBus)));
            logPrompt(baseUri, "READY", mainRouter);

            if (RestxContext.Modes.PROD.equals(getMode())) {
                // in PROD we definitely return the main router, we will never check anything else
                return mainRouter;
            } else {
                // in other modes we may record requests one by one or all of them, we use the recording decorator
                return new RecordingMainRouter(serverId, eventBus, recorder, mainRouter);
            }
        } else if (getLoadFactoryMode().equals("onrequest")) {
            logPrompt(baseUri, ">> LOAD ON REQUEST <<", null);

            RestxMainRouter router = new PerRequestFactoryLoader(serverId, eventBus);
            if (useHotCompile()) {
                router = new CompilationManagerRouter(router, eventBus);
            } else if (useHotReload()) {
                router = new HotReloadRouter(router);
            }

            return new RecordingMainRouter(serverId, eventBus, recorder, router);
        } else {
            throw new IllegalStateException("illegal load factory mode: '" + getLoadFactoryMode() + "'. " +
                    "It must be either 'onstartup' or 'onrequest'.");
        }
    }

    private static void logPrompt(String baseUri, String state, StdRestxMainRouter mainRouter) {
        logger.info("\n" +
                "--------------------------------------\n" +
                " -- RESTX " + state + " >> " + getMode().toUpperCase(Locale.ENGLISH)+ " MODE <<" +
                getHotIndicator() + "\n" +
                (mainRouter != null ? (" -- " + mainRouter.getNbFilters() + " filters\n") : "") +
                (mainRouter != null ? (" -- " + mainRouter.getNbRoutes() + " routes\n") : "") +
                (baseUri == null || baseUri.isEmpty() ? "" :
                        " -- for admin console,\n" +
                        " --   VISIT " + baseUri + "/@/ui/\n") +
                " --\n");
    }

    private static String getHotIndicator() {
        if (!RestxContext.Modes.DEV.equals(getMode())
                && !RestxContext.Modes.TEST.equals(getMode())) {
            return "";
        }
        if (useAutoCompile()) {
            return " >> AUTO COMPILE <<";
        }
        if (useHotCompile()) {
            return " >> HOT COMPILE <<";
        }
        if (useHotReload()) {
            return " >> HOT RELOAD <<";
        }
        return "";
    }

    private static Factory loadFactory(Factory.Builder builder) {
        Factory factory = builder.build();
        logger.debug("restx factory ready: {}", factory.dumper());

        return factory;
    }

    private static Factory.Builder newFactoryBuilder(String contextName, EventBus eventBus, RestxSpecRecorder specRecorder) {
        Factory.Builder builder = newFactoryBuilder(contextName, eventBus);
        if (specRecorder != null) {
            builder
                .addLocalMachines(Factory.LocalMachines.contextLocal(RestxContext.Modes.RECORDING))
                .addMachine(new SingletonFactoryMachine<>(
                        0, NamedComponent.of(RestxSpecRecorder.class, "specRecorder", specRecorder)));
        }
        return builder;
    }

    private static Factory.Builder newFactoryBuilder(String contextName, EventBus eventBus) {
        Factory.Builder builder = newFactoryBuilder(eventBus);
        if (contextName != null) {
            builder.addLocalMachines(Factory.LocalMachines.contextLocal(contextName));
        }
        return builder;
    }

    private static Factory.Builder newFactoryBuilder(EventBus eventBus) {
        return Factory.builder()
                    .addFromServiceLoader()
                    .addMachine(new SingletonFactoryMachine<>(0, NamedComponent.of(String.class, "restx.mode", getMode())))
                    .addMachine(new SingletonFactoryMachine<>(0, NamedComponent.of(EventBus.class, "eventBus", eventBus)))
                    .addLocalMachines(Factory.LocalMachines.threadLocal());
    }

    private static StdRestxMainRouter newStdRouter(Factory factory) {
        return new StdRestxMainRouter(factory.queryByClass(RestxRouting.class).findOne().get().getComponent());
    }

    private static String getLoadFactoryMode() {
        return System.getProperty("restx.factory.load",
                RestxContext.Modes.TEST.equals(getMode())
                        || RestxContext.Modes.RECORDING.equals(getMode())
                        || useHotCompile()
                        || useHotReload()
                ? "onrequest" : "onstartup");
    }

    private static boolean useHotReload() {
        return "true".equalsIgnoreCase(System.getProperty("restx.router.hotreload", "true"))
                && System.getProperty("restx.app.package") != null;
    }

    private static boolean useHotCompile() {
        return "true".equalsIgnoreCase(System.getProperty("restx.router.hotcompile", "true"))
                && System.getProperty("restx.app.package") != null
                && hasToolsJar();
    }

    private static boolean hasToolsJar() {
        try {
            Class.forName("javax.tools.ToolProvider");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean useAutoCompile() {
        return "true".equalsIgnoreCase(System.getProperty("restx.router.autocompile", "true"))
                && useHotCompile();
    }


    private RestxMainRouterFactory() {}
}
