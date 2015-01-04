package restx;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Stopwatch;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
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
import restx.common.metrics.api.MetricRegistry;
import restx.factory.Factory;
import restx.factory.Name;
import restx.factory.NamedComponent;
import restx.factory.SingletonFactoryMachine;
import restx.factory.StdWarehouse;
import restx.factory.Warehouse;
import restx.http.HttpStatus;
import restx.security.RestxSessionCookieFilter;
import restx.server.WebServer;
import restx.server.WebServers;
import restx.specs.RestxSpec;
import restx.specs.RestxSpecRecorder;
import restx.specs.RestxSpecRecorder.GivenRecorder;
import restx.specs.RestxSpecTape;

import javax.tools.Diagnostic;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.getLast;
import static restx.common.MoreStrings.indent;

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
            factory.get().close();
            Factory.unregister(serverId, factory.get());
        }
    }

    /**
     * A blade is a context of test execution, it can be reused between tests, but can't be used concurrently.
     *
     * This allow to have a test client used to run tests concurrently, each thread executing tests being
     * considered as a test blade. It allows to have some heavy components (like a database) initialized
     * only once per blade.
     *
     * This works only when using a per request factory loading.
     */
    public static final class Blade {
        private static final AtomicLong BLADE_COUNTER = new AtomicLong();

        private static final ThreadLocal<String> BLADE = new ThreadLocal<String>() {
            @Override
            protected String initialValue() {
                return "BLADE-" + BLADE_COUNTER.incrementAndGet();
            }
        };

        public static String current() {
            return BLADE.get();
        }

        public static String contextId(String serverId, String bladeId) {
            return serverId + "-" + bladeId;
        }

        public static Factory.LocalMachines bladeLocalMachines(String serverId) {
            return Factory.LocalMachines.contextLocal(contextId(serverId, current()));
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
        private final RestxMainRouter router;
        private final String serverId;
        private final RestxSpec.Storage storage;
        private final RestxSpec.StorageSettings storageSettings;
        private final RestxSpecRecorder.Repository repository;

        public RecordingMainRouter(String serverId,
                                   RestxMainRouter router,
                                   RestxSpec.StorageSettings storageSettings) {
            this.serverId = serverId;
            this.router = router;
            this.storageSettings = storageSettings;
            this.storage = RestxSpec.Storage.with(storageSettings);
            this.repository = new RestxSpecRecorder.Repository();
        }


        @Override
        public void route(final RestxRequest restxRequest, final RestxResponse restxResponse) throws IOException {
            Factory.LocalMachines.threadLocal().set("RecordedSpecsRepository", repository);

            if (!restxRequest.getRestxPath().startsWith("/@/")
                    && RestxContext.Modes.RECORDING.equals(getMode(restxRequest))) {
                logger.debug("RECORDING {}", restxRequest);

                Factory factory = Factory.newInstance();
                Set<GivenRecorder> recorders = factory.getComponents(RestxSpecRecorder.GivenRecorder.class);
                RestxSessionCookieFilter sessionFilter = factory.getComponent(RestxSessionCookieFilter.class);
                final RestxSpecRecorder restxSpecRecorder = new RestxSpecRecorder(
                                                                recorders, sessionFilter, storageSettings, repository);
                try {
                    Optional<String> recordPath = restxRequest.getHeader("RestxRecordPath");
                    RestxSpecTape tape = restxSpecRecorder.record(restxRequest, restxResponse,
                            recordPath, restxRequest.getHeader("RestxRecordTitle"));
                    try {
                        router.route(tape.getRecordingRequest(), tape.getRecordingResponse());
                    } finally {
                        RestxSpecRecorder.RecordedSpec recordedSpec = restxSpecRecorder.stop(tape);

                        if (recordPath.isPresent()) {
                            if (recordedSpec.getSpec() == null) {
                                logger.warn("can't save spec, not properly recorded for {}", restxRequest);
                            } else {
                                // save directly the recorded spec
                                File recordFile = storage.store(recordedSpec.getSpec());
                                logger.info("saved recorded spec in {}", recordFile);
                            }
                        }
                    }
                } catch (IOException e) {
                    throw e;
                } catch (Exception e) {
                    Throwables.propagate(e);
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
            Stopwatch stopwatch = Stopwatch.createStarted();
            Factory factory = loadFactory(newFactoryBuilder(
                                        serverId,
                                        restxRequest.getHeader("RestxBlade"),
                                        restxRequest.getHeader("RestxThreadLocal"),
                                        getMode(restxRequest))
                    .addWarehouseProvider(warehouse));

            if ("cleanrequest".equals(getLoadFactoryMode())) {
                // in clean request mode auto startable components are started and cleaned per request
                try {
                    factory.start();
                } catch (Exception ex) {
                    logger.error("Exception when using factory to start components on clean request: " + ex.getMessage(), ex);
                    Throwables.propagate(ex);
                }
            }

            factory.prepare();

            try {
                StdRestxMainRouter stdRestxMainRouter = newStdRouter(factory);
                if (stopwatch.elapsed(TimeUnit.MILLISECONDS) > 200) {
                    logger.info("{} - per request factory created in {}", restxRequest, stopwatch);
                } else {
                    logger.debug("{} - per request factory created in {}", restxRequest, stopwatch);
                }
                stdRestxMainRouter.route(restxRequest, restxResponse);
            } catch (Factory.UnsatisfiedDependenciesException ex) {
                if (restxRequest.getHeader("RestxDebug").isPresent()) {
                    logger.error("{} - Exception when using factory to load router: {}\n{}",
                            restxRequest, ex.getMessage(), factory.dumper());
                } else {
                    logger.error("{} - Exception when using factory to load router: {}\n" +
                            "Pro Tip: Set HTTP Header RestxDebug to have a dump of the factory" +
                            " in your logs when you get this error.", restxRequest, ex.getMessage());
                }
                throw ex;
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
        private final Supplier<ImmutableSet<Class>> coldClasses;

        public HotReloadRouter(RestxMainRouter delegate, Supplier<ImmutableSet<Class>> coldClasses) {
            this.delegate = delegate;
            this.coldClasses = Suppliers.memoize(coldClasses);
            this.rootPackage = appSettings.appPackage().get();
        }

        @Override
        public void route(RestxRequest restxRequest, RestxResponse restxResponse) throws IOException {
            ClassLoader previousLoader =
                    Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(new HotReloadingClassLoader(
                        previousLoader, rootPackage, coldClasses.get()));
                delegate.route(restxRequest, restxResponse);
            } catch (Factory.UnsatisfiedDependenciesException ex) {
                handleUnsatisfiedDependencyOnHotReload(restxResponse, ex, rootPackage);
            } finally {
                Thread.currentThread().setContextClassLoader(previousLoader);
            }
        }
    }

    private class CompilationManagerRouter implements RestxMainRouter {
        private final RestxMainRouter delegate;
        private final String rootPackage;
        private final CompilationManager compilationManager;
        private final Supplier<ImmutableSet<Class>> coldClasses;
        private ClassLoader classLoader;

        public CompilationManagerRouter(RestxMainRouter delegate, EventBus eventBus,
                                        Supplier<ImmutableSet<Class>> coldClasses, CompilationSettings compilationSettings) {
            this.delegate = delegate;
            this.coldClasses = Suppliers.memoize(coldClasses);
            this.rootPackage = appSettings.appPackage().get();
            compilationManager = Apps.with(appSettings).newAppCompilationManager(eventBus, compilationSettings);
            eventBus.register(new Object() {
                @Subscribe
                public void onCompilationFinished(
                        CompilationFinishedEvent event) {
                    synchronized (CompilationManagerRouter.this) {
                        classLoader = null;
                    }
                }
            });
            compilationManager.incrementalCompile();
            if (useAutoCompile()) {
                compilationManager.startAutoCompile();
            }
        }

        private void setClassLoader() {
            classLoader = compilationManager.newHotReloadingClassLoader(rootPackage, coldClasses.get());
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

                synchronized (this) {
                    if (classLoader == null) {
                        setClassLoader();
                    }
                }

                Thread.currentThread().setContextClassLoader(classLoader);
                delegate.route(restxRequest, restxResponse);
            } catch (Factory.UnsatisfiedDependenciesException ex) {
                handleUnsatisfiedDependencyOnHotReload(restxResponse, ex, rootPackage);
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

        if (getLoadFactoryMode().equals("onstartup")) {
            if (RestxContext.Modes.RECORDING.equals(getMode())) {
                throw new IllegalStateException("can't use RECORDING mode without per request factory loading");
            }

            Factory factory = loadFactory(newFactoryBuilder(serverId));
            factory = Factory.register(serverId, factory);

            StdRestxMainRouter mainRouter = newStdRouter(factory);
            routers.put(serverId, mainRouter);
            factory.start().and().prepare();

            logPrompt(baseUri, "READY", mainRouter);

            return mainRouter;
        } else if (getLoadFactoryMode().equals("onrequest")
                || getLoadFactoryMode().equals("cleanrequest")) {
            logPrompt(baseUri, ">> LOAD ON REQUEST <<"
                    + (getLoadFactoryMode().equals("cleanrequest") ? " >> CLEAN <<" : ""), null);


            ClassLoader previous = Thread.currentThread().getContextClassLoader();
            if (useAutoCompile()) {
                CompilationManager compilationManager = Apps.with(appSettings).newAppCompilationManager(
                                                        new EventBus(), CompilationManager.DEFAULT_SETTINGS);
                compilationManager.incrementalCompile();
                Thread.currentThread().setContextClassLoader(
                        compilationManager.newHotReloadingClassLoader(
                                appSettings.appPackage().get(), ImmutableSet.<Class>of()));
            }

            // Create a Factory to load autotartable components
            // then one factory will be created for each request.
            // The warehouse of this first factory will be shared among all factories created for this router,
            // making autostartable components live tied to the router itself and not per request

            // in 'cleanrequest' mode, this factory is not actually used, autostartable components
            // are started per request
            Factory factory;
            try {
                factory = loadFactory(newFactoryBuilder(serverId));
                factory = Factory.register(serverId, factory);
            } finally {
                Thread.currentThread().setContextClassLoader(previous);
            }

            Warehouse warehouse = getLoadFactoryMode().equals("cleanrequest") ? new StdWarehouse() : factory.getWarehouse();

            RestxMainRouter router = new PerRequestFactoryLoader(serverId, warehouse);


            // this factory is used to look up settings only
            // we don't use 'factory' instance to avoid having settings built into the autostartable factory
            Factory settingsFactory = loadFactory(newFactoryBuilder(serverId));

            // wrap in a recording router, as any request may ask for recording with RestxMode header
            router = new RecordingMainRouter(serverId, router,
                    settingsFactory.getComponent(RestxSpec.StorageSettings.class));

            // wrap in hot reloading or hoy compile router if needed.
            // this must be the last wrapping so that the classloader is used for the full request, including
            // for recording
            if (useHotCompile()) {
                final RestxConfig config = settingsFactory.getComponent(RestxConfig.class);
                router = new CompilationManagerRouter(router, factory.getComponent(EventBus.class),
                        getColdClasses(factory),
                        new CompilationSettings() {
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
                router = new HotReloadRouter(router, getColdClasses(factory));
            }

            routers.put(serverId, router);
            if (getLoadFactoryMode().equals("onrequest")) {
                factory.start();
            }

            return router;
        } else {
            throw new IllegalStateException("illegal load factory mode: '" + getLoadFactoryMode() + "'. " +
                    "It must be either 'onstartup', 'onrequest' or 'cleanrequest'.");
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

    private static Factory.Builder newFactoryBuilder(String serverId,
                                                     Optional<String> bladeId,
                                                     Optional<String> threadLocalId,
                                                     String mode) {
        Factory.Builder builder = newFactoryBuilder(serverId);
        if (threadLocalId.isPresent()) {
            builder.addLocalMachines(Factory.LocalMachines.threadLocalFrom(threadLocalId.get()));
        }
        if (bladeId.isPresent()) {
            builder.addLocalMachines(Factory.LocalMachines.contextLocal(Blade.contextId(serverId, bladeId.get())));
        }
        builder.addMachine(new SingletonFactoryMachine<>(
                -100000, NamedComponent.of(String.class, "restx.mode", mode)));

        return builder;
    }

    private static Factory.Builder newFactoryBuilder(String serverId) {
        Factory.Builder builder = newFactoryBuilder();
        if (serverId != null) {
            builder.addLocalMachines(Factory.LocalMachines.contextLocal(serverId));

            // make server id and base url accessible in the factory components
            Optional<WebServer> serverById = WebServers.getServerById(serverId);
            builder
                    .addMachine(new SingletonFactoryMachine<>(
                            0, NamedComponent.of(String.class, "restx.server.id", serverId)))
                    .addMachine(new SingletonFactoryMachine<>(
                            0, NamedComponent.of(String.class, "restx.server.baseUrl",
                            serverById.isPresent() ? serverById.get().baseUrl() : "")))
                    .addMachine(new SingletonFactoryMachine<>(
                            0, NamedComponent.of(String.class, "restx.server.port",
                            serverById.isPresent() ? String.valueOf(serverById.get().getPort()) : "")))
                    .addMachine(new SingletonFactoryMachine<>(
                            0, NamedComponent.of(String.class, "restx.server.type",
                            serverById.isPresent() ? serverById.get().getServerType() : "")))
            ;
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
        if (appSettings.factoryLoadMode().isPresent()) {
            return appSettings.factoryLoadMode().get();
        }

        if (RestxContext.Modes.TEST.equals(getMode())
                || RestxContext.Modes.INFINIREST.equals(getMode())) {
            return "cleanrequest";
        } else if (RestxContext.Modes.RECORDING.equals(getMode())
                || useHotCompile()
                || useHotReload()) {
            return "onrequest";
        } else {
            return "onstartup";
        }
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
                && !getMode().equals(RestxContext.Modes.PROD)
                && !getMode().equals(RestxContext.Modes.TEST)
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
            } else if (!hasSystemJavaCompiler()) {
                logger.info("can't enable hot compile: tools.jar is not in classpath.\n" +
                        "Run your app with a JDK rather than a JRE to enable hot compile.");
                return false;
            } else {
                return true;
            }
        } else {
            return appSettings.hotCompile().or(Boolean.TRUE)
                && !getMode().equals(RestxContext.Modes.PROD)
                && !getMode().equals(RestxContext.Modes.TEST)
                && appSettings.appPackage().isPresent()
                && hasSystemJavaCompiler();
        }
    }

    private String getMode() {
        return appSettings.mode();
    }

    private String getMode(RestxRequest restxRequest) {
        return restxRequest.getHeader("RestxMode").or(getMode());
    }

    private boolean hasSystemJavaCompiler() {
        return Apps.hasSystemJavaCompiler();
    }

    private boolean useAutoCompile() {
        return appSettings.autoCompile().or(Boolean.TRUE)
                && useHotCompile();
    }

    private static void handleUnsatisfiedDependencyOnHotReload(
            RestxResponse restxResponse, Factory.UnsatisfiedDependenciesException ex, String hotPackage) throws IOException {
        restxResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        PrintWriter writer = restxResponse.getWriter();

        // let's check one possible cause which is hard to understand: circular dependencies between
        // hot and cold classes - ie classes part of hot reload and classes outside hot reload
        boolean hotColdFound = false;
        for (Factory.UnsatisfiedDependency unsatisfiedDependency :
                ex.getUnsatisfiedDependencies().getUnsatisfiedDependencies()) {
            if (unsatisfiedDependency.getPath().isEmpty()) {
                continue;
            }
            Class<?> dependerClass = getLast(unsatisfiedDependency.getPath()).getName().getClazz();
            Class<?> dependeeClass = unsatisfiedDependency.getUnsatisfied().getComponentClass();
            if (!dependerClass.getName().startsWith(hotPackage)
                    && dependeeClass.getName().startsWith(hotPackage)) {
                // we have found a dependency from cold to hot class
                String msg = String.format(">>>>>> SOURCE CODE ERROR >>>>>>>>>>>>>>>>>>>>>>>>>>>>\n" +
                        "You are currently using hot reload feature of RESTX which has some limitations.\n\n" +
                        "You can't inject a component which is hot reloaded (called a 'Hot' component)\n" +
                        "  into a component which is not hot reloaded (called a 'Cold' component)\n" +
                        "\n" +
                        "Such a dependency from a 'Cold' class to a 'Hot' class has been found in your sources:\n\n" +
                        "     `%s`\n" +
                        "          ^------------------------------------- HOT because it is in package `%s`\n\n" +
                        "                       is injected into\n\n" +
                        "     `%s`\n" +
                        "          ^------------------------------------- COLD because it is NOT in package `%s`\n" +
                        "\n\n" +
                        "                >>> THIS IS NOT SUPPORTED, IT CAUSES CLASSLOADING ERRORS <<<\n" +
                        "\n\n" +
                        "Possible solutions:\n" +
                        "===================\n\n" +
                        "1) remove that dependency\n" +
                        "      Check the source of `%s`\n" +
                        "      and remove its dependency on `%s`\n\n" +
                        "2) change which classes are hot reloaded\n" +
                        "      Classes which are hot reloaded are in package `%s`.\n" +
                        "      You can change that by setting the `restx.app.package` system property.\n\n" +
                        "3) don't use hot compile mode\n" +
                        "      Use production mode\n" +
                        "      or explicitly disable it by setting `restx.router.hotcompile`\n" +
                        "                                 and / or `restx.router.hotreload` to false\n\n" +
                        ">>>>>> SOURCE CODE ERROR >>>>>>>>>>>>>>>>>>>>>>>>>>>>\n\n",
                        dependeeClass.getName(), hotPackage,
                        dependerClass.getName(), hotPackage,
                        dependerClass.getName(), dependeeClass.getName(),
                        hotPackage);
                logger.error("\n\n" + msg);
                writer.println(msg);
                hotColdFound = true;
                break;
            }
        }

        if (!hotColdFound) {
            String msg =
                    "Error when loading Factory to process your request.\n" +
                            "One or more dependency injections can be sastifed.\n\n" +
                            indent(ex.getMessage(), 2);
            logger.error(msg);
            writer.println(msg);
        }
    }

    private static Supplier<ImmutableSet<Class>> getColdClasses(final Factory factory) {
        return new Supplier<ImmutableSet<Class>>() {
            @Override
            public ImmutableSet<Class> get() {
                Collection<Class> coldClasses = new HashSet<>();
                for (Name<?> name : factory.getWarehouse().listNames()) {
                    Optional<?> c = factory.queryByName(name).findOneAsComponent();
                    if (c.isPresent()) {
                        coldClasses.add(c.get().getClass());
                    } else {
                        logger.debug(
                                "invalid cold class {}: found in factory warehouse but not available as component." +
                                " Ignored.", name);
                    }
                }

                return ImmutableSet.copyOf(coldClasses);
            }
        };
    }

}
