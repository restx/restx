package restx.tests;

import com.google.common.base.Optional;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import restx.factory.Factory;
import restx.server.WebServerSupplier;
import restx.specs.RestxSpec;
import restx.specs.RestxSpecLoader;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static restx.factory.Factory.LocalMachines;
import static restx.factory.Factory.LocalMachines.contextLocal;

/**
 * User: xavierhanin
 * Date: 3/17/13
 * Time: 1:58 PM
 */
public class RestxSpecRule extends RestxServerRule {

    public static Factory defaultFactory() {
        return Factory.builder()
                .addLocalMachines(LocalMachines.threadLocal())
                .addLocalMachines(contextLocal(RestxSpecRule.class.getSimpleName()))
                .addFromServiceLoader()
                .build();
    }

    private final String routerPath;
    private final RestxSpecLoader specLoader = new RestxSpecLoader();

    private final Iterable<GivenSpecRule> givenSpecRules;
    private final Iterable<GivenRunner> givenRunners;
    private final Iterable<WhenChecker> whenCheckers;


    /**
     * A shortcut for new RestxSpecRule("src/main/webapp/WEB-INF/web.xml", "src/main/webapp",
     *                          "/api", jettyWebServerSupplier(webInfLocation, appBase), defaultFactory())
     */
    public RestxSpecRule() {
        this("src/main/webapp/WEB-INF/web.xml", "src/main/webapp");
    }

    /**
     * A shortcut for new RestxSpecRule("/api", jettyWebServerSupplier(webInfLocation, appBase), defaultFactory())
     */
    public RestxSpecRule(String webInfLocation, String appBase) {
        this(webInfLocation, appBase, defaultFactory());
    }

    /**
     * A shortcut for new RestxSpecRule("/api", jettyWebServerSupplier(webInfLocation, appBase), factory)
     */
    public RestxSpecRule(final String webInfLocation, final String appBase, Factory factory) {
        this("/api", jettyWebServerSupplier(webInfLocation, appBase), factory);
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
        givenSpecRules = newArrayList(transform(factory.queryByClass(GivenSpecRuleSupplier.class).findAsComponents(),
                Suppliers.<GivenSpecRule>supplierFunction()));
        givenRunners = factory.queryByClass(GivenRunner.class).findAsComponents();
        whenCheckers = factory.queryByClass(WhenChecker.class).findAsComponents();
    }

    public void runTest(String spec) throws IOException {
        runTest(loadSpec(spec));
    }

    public RestxSpec loadSpec(String spec) throws IOException {
        return specLoader.load(spec);
    }

    public void runTest(RestxSpec restxSpec) {
        Map<String, String> params = Maps.newLinkedHashMap();
        for (GivenSpecRule givenSpecRule : givenSpecRules) {
            params.putAll(givenSpecRule.getRunParams());
        }
        params.put(RestxSpec.WhenHttpRequest.CONTEXT_NAME, server.getServerId());
        params.put(RestxSpec.WhenHttpRequest.BASE_URL, server.baseUrl() + routerPath);

        runSpec(restxSpec, ImmutableMap.copyOf(params));
    }

    private void runSpec(RestxSpec restxSpec, ImmutableMap<String, String> params) {
        List<GivenCleaner> givenCleaners = newArrayList();
        try {
            for (RestxSpec.Given given : restxSpec.getGiven()) {
                Optional<GivenRunner> runnerFor = findRunnerFor(given);
                if (!runnerFor.isPresent()) {
                    throw new IllegalStateException(
                            "no runner found for given " + given + ". double check your classpath and factory settings.");
                }
                givenCleaners.add(runnerFor.get().run(given, params));
            }

            for (RestxSpec.When when : restxSpec.getWhens()) {
                Optional<WhenChecker> checkerFor = findCheckerFor(when);
                if (!checkerFor.isPresent()) {
                    throw new IllegalStateException("no checker found for when " + when + "." +
                            " double check your classpath and factory settings.");
                }
                checkerFor.get().check(when, params);
            }
        } finally {
            for (GivenCleaner givenCleaner : givenCleaners) {
                givenCleaner.cleanUp();
            }
        }

    }

    private Optional<WhenChecker> findCheckerFor(RestxSpec.When when) {
        if (when instanceof WhenChecker) {
            return Optional.of((WhenChecker) when);
        }
        for (WhenChecker whenChecker : whenCheckers) {
            if (whenChecker.getWhenClass().isAssignableFrom(when.getClass())) {
                return Optional.of(whenChecker);
            }
        }

        return Optional.absent();
    }

    private Optional<GivenRunner> findRunnerFor(RestxSpec.Given given) {
        if (given instanceof GivenRunner) {
            return Optional.of((GivenRunner) given);
        }
        for (GivenRunner givenRunner : givenRunners) {
            if (givenRunner.getGivenClass().isAssignableFrom(given.getClass())) {
                return Optional.of(givenRunner);
            }
        }

        return Optional.absent();
    }

    @Override
    protected void afterServerCreated() {
        for (GivenSpecRule givenSpecRule : givenSpecRules) {
            givenSpecRule.onSetup(localMachines());
        }
    }

    @Override
    protected void beforeServerStop() {
        for (GivenSpecRule givenSpecRule : givenSpecRules) {
            givenSpecRule.onTearDown(localMachines());
        }
    }
}
