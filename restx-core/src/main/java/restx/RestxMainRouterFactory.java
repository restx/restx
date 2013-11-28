package restx;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.net.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.classloader.CompilationFinishedEvent;
import restx.classloader.CompilationManager;
import restx.classloader.CompilationSettings;
import restx.classloader.HotReloadingClassLoader;
import restx.common.RestxConfig;
import restx.factory.*;
import restx.http.HttpStatus;
import restx.specs.RestxSpec;
import restx.specs.RestxSpecRecorder;
import restx.specs.RestxSpecTape;

import javax.tools.Diagnostic;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: xavierhanin
 * Date: 2/16/13
 * Time: 3:40 PM
 */
public class RestxMainRouterFactory {
    private static final Logger logger = LoggerFactory.getLogger(RestxMainRouterFactory.class);
    private static final Map<String, RestxMainRouter> routers = new HashMap<>();

    public static synchronized RestxMainRouter newInstance(final String serverId, Optional<String> baseUri) {
        checkNotNull(serverId);
        RestxMainRouter router = routers.get(serverId);
        if (router == null)  {
            AppSettings settings = loadFactory(newFactoryBuilder(serverId))
                    .getComponent(AppSettings.class);

            router = new RestxMainRouterFactory(settings).build(serverId, baseUri);
            routers.put(serverId, router);
        }
        return router;
    }

    public static synchronized Optional<RestxMainRouter> getInstance(String serverId) {
        return Optional.fromNullable(routers.get(serverId));
    }

    public static synchronized void clear(String serverId) {
        routers.remove(serverId);
        Optional<Factory> factory = Factory.getFactory(serverId);
        if (factory.isPresent()) {
            Factory.unregister(serverId, factory.get());
        }
    }

    /**
     * A main router decorator that may record all or some requests.
     *
     * Note that the provided router is only used for non recorded requests.
     * The recorded requests are using a StdRestxMainRouter with a factory initialized per request,
     * to allow recorders to be properly setup in the router factory.
     *
     * Therefore this class is not intended to be exposed outside the main router factory class.
     */
    private class RecordingMainRouter implements RestxMainRouter {
        private final Optional<RestxSpecRecorder> restxSpecRecorder;
        private final RestxMainRouter router;
        private final String serverId;
        private final RestxSpec.Storage storage;
        private final Supplier<RestxSpecRecorder> recorderSupplier = new Supplier<RestxSpecRecorder>() {
            @Override
            public RestxSpecRecorder get() {
                RestxSpecRecorder recorder = new RestxSpecRecorder();
                recorder.install();
                return recorder;
            }
        };

        public RecordingMainRouter(String serverId, Optional<RestxSpecRecorder> restxSpecRecorder,
                                   RestxMainRouter router, RestxSpec.StorageSettings storageSettings) {
            this.serverId = serverId;
            this.restxSpecRecorder = restxSpecRecorder;
            this.router = router;
            this.storage = RestxSpec.Storage.with(storageSettings);
        }


        @Override
        public void route(final RestxRequest restxRequest, final RestxResponse restxResponse) throws IOException {
            if (!restxRequest.getRestxPath().startsWith("/@/")
                    && (restxSpecRecorder.isPresent()
                    || RestxContext.Modes.RECORDING.equals(getMode(restxRequest)))) {
                logger.debug("RECORDING {}", restxRequest);

                final RestxSpecRecorder restxSpecRecorder = this.restxSpecRecorder.or(recorderSupplier);
                try {
                    RestxSpecRecorder.doWithRecorder(restxSpecRecorder, new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            Optional<String> recordPath = restxRequest.getHeader("RestxRecordPath");
                            RestxSpecTape tape = restxSpecRecorder.record(restxRequest, restxResponse,
                                    recordPath, restxRequest.getHeader("RestxRecordTitle"));
                            try {
                                // when recording a request we don't use the provided router, we
                                // need to load a new factory, some recorders rely on being available in the factory
                                Factory factory = loadFactory(newFactoryBuilder(serverId,
                                                                                restxSpecRecorder, getMode(restxRequest)));
                                try {
                                    newStdRouter(factory).route(tape.getRecordingRequest(), tape.getRecordingResponse());
                                } finally {
                                    factory.close();
                                }
                            } finally {
                                RestxSpecRecorder.RecordedSpec recordedSpec = restxSpecRecorder.stop(tape);

                                if (recordPath.isPresent()) {
                                    // save directly the recorded spec
                                    File recordFile = storage.store(recordedSpec.getSpec());
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

    private class PerRequestFactoryLoader implements RestxMainRouter {
        private final String serverId;
        private final Warehouse warehouse;

        public PerRequestFactoryLoader(String serverId, Warehouse warehouse) {
            this.serverId = serverId;
            this.warehouse = warehouse;
        }

        @Override
        public void route(RestxRequest restxRequest, RestxResponse restxResponse) throws IOException {
            Factory factory = loadFactory(newFactoryBuilder(serverId,
                    RestxSpecRecorder.current().orNull(), getMode(restxRequest))
                    .addWarehouseProvider(warehouse));

            try {
                newStdRouter(factory).route(restxRequest, restxResponse);
            } catch (IllegalStateException ex) {
                logger.info("Exception when using factory to load router: {}\n{}", ex.getMessage(), factory.dumper());
            } finally {
                factory.close();
            }
        }
    }

    /**
     * Enables hot reload: this rely on classes compiled by an external compiler
     */
    private class HotReloadRouter implements RestxMainRouter {
        private final RestxMainRouter delegate;
        private final String rootPackage;

        public HotReloadRouter(RestxMainRouter delegate) {
            this.delegate = delegate;
            this.rootPackage = appSettings.appPackage().get();
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

    private class CompilationManagerRouter implements RestxMainRouter {
        private final RestxMainRouter delegate;
        private final String rootPackage;
        private final Path destinationDir;
        private final CompilationManager compilationManager;
        private ClassLoader classLoader;

        public CompilationManagerRouter(RestxMainRouter delegate, EventBus eventBus, CompilationSettings compilationSettings) {
            this.delegate = delegate;
            this.rootPackage = appSettings.appPackage().get();
            compilationManager = Apps.with(appSettings).newAppCompilationManager(eventBus, compilationSettings);
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
            try {
                classLoader = new HotReloadingClassLoader(
                        new URLClassLoader(
                                new URL[] {destinationDir.toUri().toURL()},
                                Thread.currentThread().getContextClassLoader()), rootPackage) {
                    protected InputStream getInputStream(String path) {
                        try {
                            return Files.newInputStream(destinationDir.resolve(path));
                        } catch (IOException e) {
                            return null;
                        }
                    }
                };
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
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
                    restxResponse.setStatus(HttpStatus.SERVICE_UNAVAILABLE);
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

                Thread.currentThread().setContextClassLoader(classLoader);
                delegate.route(restxRequest, restxResponse);
            } finally {
                Thread.currentThread().setContextClassLoader(previousLoader);
            }
        }
    }

    private final AppSettings appSettings;

    private RestxMainRouterFactory(AppSettings appSettings) {
        this.appSettings = appSettings;
    }

    private RestxMainRouter build(final String serverId, Optional<String> baseUri) {
        checkNotNull(serverId);
        checkNotNull(baseUri);

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

        Optional<RestxSpecRecorder> recorder;
        if (RestxContext.Modes.RECORDING.equals(getMode())) {
            recorder = Optional.of(new RestxSpecRecorder());
            recorder.get().install();
        } else {
            recorder = Optional.absent();
        }

        // this factory is used to handle autostartable components at least,
        // then one factory may be created for each request
        // its warehouse will be shared among all factories created for this router,
        // making autostartable components live tied to the router
        Factory factory = loadFactory(newFactoryBuilder(serverId)).start();
        factory = Factory.register(serverId, factory);

        if (getLoadFactoryMode().equals("onstartup")) {
            final StdRestxMainRouter mainRouter = newStdRouter(factory);
            logPrompt(baseUri, "READY", mainRouter);

            if (RestxContext.Modes.PROD.equals(getMode())) {
                // in PROD we definitely return the main router, we will never check anything else
                return mainRouter;
            } else {
                // in other modes we may record requests one by one or all of them, we use the recording decorator
                return new RecordingMainRouter(serverId, recorder, mainRouter,
                        factory.getComponent(RestxSpec.StorageSettings.class));
            }
        } else if (getLoadFactoryMode().equals("onrequest")) {
            logPrompt(baseUri, ">> LOAD ON REQUEST <<", null);


            RestxMainRouter router = new PerRequestFactoryLoader(serverId, factory.getWarehouse());


            // this factory is used to look up settings only
            // we don't use 'factory' instance to avoid having settings built into the
            Factory settingsFactory = loadFactory(newFactoryBuilder(serverId)
                                            .addWarehouseProvider(factory.getWarehouse()));

            // wrap in a recording router, as any request may ask for recording with RestxMode header
            router = new RecordingMainRouter(serverId, recorder, router,
                    settingsFactory.getComponent(RestxSpec.StorageSettings.class));

            // wrap in hot reloading or hoy compile router if needed.
            // this must be the last wrapping so that the classloader is used for the full request, including
            // for recording
            if (useHotCompile()) {
                final RestxConfig config = settingsFactory.getComponent(RestxConfig.class);
                router = new CompilationManagerRouter(router, factory.getComponent(EventBus.class), new CompilationSettings() {
                    @Override
                    public int autoCompileCoalescePeriod() {
                        return config.getInt("restx.fs.watch.coalesce.period").get();
                    }

                    @Override
                    public Predicate<Path> classpathResourceFilter() {
                        return CompilationManager.DEFAULT_CLASSPATH_RESOURCE_FILTER;
                    }
                });
            } else if (useHotReload()) {
                router = new HotReloadRouter(router);
            }

            return router;
        } else {
            throw new IllegalStateException("illegal load factory mode: '" + getLoadFactoryMode() + "'. " +
                    "It must be either 'onstartup' or 'onrequest'.");
        }
    }

    private void logPrompt(Optional<String> baseUri, String state, StdRestxMainRouter mainRouter) {
        logger.info("\n" +
                "--------------------------------------\n" +
                " -- RESTX " + state + " >> " + getMode().toUpperCase(Locale.ENGLISH)+ " MODE <<" +
                getHotIndicator() + "\n" +
                (mainRouter != null ? (" -- " + mainRouter.getNbFilters() + " filters\n") : "") +
                (mainRouter != null ? (" -- " + mainRouter.getNbRoutes() + " routes\n") : "") +
                (baseUri.or("").isEmpty() ? "" :
                        " -- for admin console,\n" +
                        " --   VISIT " + baseUri.get() + "/@/ui/\n") +
                " --\n");
    }

    private String getHotIndicator() {
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

    private static Factory.Builder newFactoryBuilder(String contextName,
                                                     RestxSpecRecorder specRecorder, String mode) {
        Factory.Builder builder = newFactoryBuilder(contextName);
        if (specRecorder != null) {
            builder
                .addLocalMachines(Factory.LocalMachines.contextLocal(RestxContext.Modes.RECORDING))
                    .addMachine(new SingletonFactoryMachine<>(
                            0, NamedComponent.of(RestxSpecRecorder.class, "specRecorder", specRecorder)))
                    .addMachine(new SingletonFactoryMachine<>(
                            -100000, NamedComponent.of(String.class, "restx.mode", mode)))
            ;
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
                    .addLocalMachines(Factory.LocalMachines.threadLocal())
                ;
    }

    private StdRestxMainRouter newStdRouter(Factory factory) {
        return new StdRestxMainRouter(
                factory.getComponent(MetricRegistry.class),
                factory.getComponent(RestxRouting.class),
                factory.getComponent(Name.of(String.class, "restx.mode")));
    }

    private String getLoadFactoryMode() {
        return appSettings.factoryLoadMode().or(
                RestxContext.Modes.TEST.equals(getMode())
                    || RestxContext.Modes.RECORDING.equals(getMode())
                    || useHotCompile()
                    || useHotReload()
                ? "onrequest" : "onstartup");
    }

    private boolean useHotReload() {
        if (appSettings.hotReload().or(Boolean.FALSE)) {
            // hotreload is explicitly set
            if (!appSettings.appPackage().isPresent()) {
                logger.info("can't enable hot reload: restx.app.package is not set.\n" +
                        "Run your app with -Drestx.app.package=<app.base.package> to enable hot reload.");
                return false;
            } else {
                return true;
            }
        } else {
            return appSettings.hotReload().or(Boolean.TRUE)
                && !getMode().equals("prod")
                && appSettings.appPackage().isPresent();
        }
    }

    private boolean useHotCompile() {
        if (appSettings.hotCompile().or(Boolean.FALSE)
                || appSettings.autoCompile().or(Boolean.FALSE)) {
            // hotcompile or autocompile is explicitly set
            if (!appSettings.appPackage().isPresent()) {
                logger.info("can't enable hot compile: restx.app.package is not set.\n" +
                        "Run your app with -Drestx.app.package=<app.base.package> to enable hot compile.");
                return false;
            } else if (!hasToolsJar()) {
                logger.info("can't enable hot compile: tools.jar is not in classpath.\n" +
                        "Run your app with a JDK rather than a JRE to enable hot compile.");
                return false;
            } else {
                return true;
            }
        } else {
            return appSettings.hotCompile().or(Boolean.TRUE)
                && !getMode().equals("prod")
                && appSettings.appPackage().isPresent()
                && hasToolsJar();
        }
    }

    private String getMode() {
        return appSettings.mode();
    }

    private String getMode(RestxRequest restxRequest) {
        return restxRequest.getHeader("RestxMode").or(getMode());
    }

    private boolean hasToolsJar() {
        try {
            Class.forName("javax.tools.ToolProvider");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean useAutoCompile() {
        return appSettings.autoCompile().or(Boolean.TRUE)
                && useHotCompile();
    }

}
