package restx.tests;

import com.google.common.base.Optional;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import restx.factory.Factory;
import restx.specs.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static restx.factory.Factory.LocalMachines;
import static restx.factory.Factory.LocalMachines.contextLocal;

/**
 * User: xavierhanin
 * Date: 3/17/13
 * Time: 1:58 PM
 */
public class RestxSpecRunner {

    private final String serverId;
    private final String baseUrl;

    public static Factory defaultFactory() {
        return Factory.builder()
                .addLocalMachines(LocalMachines.threadLocal())
                .addLocalMachines(contextLocal(RestxSpecRunner.class.getSimpleName()))
                .addFromServiceLoader()
                .build();
    }

    private final String routerPath;
    private final RestxSpecLoader specLoader;

    private final Iterable<GivenSpecRule> givenSpecRules;
    private final Iterable<GivenRunner> givenRunners;
    private final Iterable<WhenChecker> whenCheckers;


    /**
     * Constructs a new RestxSpecRunner.
     *
     * @param routerPath the path at which restx router is mounted. eg '/api'
     * @param factory the restx Factory to use to find GivenSpecRuleSupplier s when executing the spec.
     *                This is not used for the server itself.
     */
    public RestxSpecRunner(String routerPath, String serverId, String baseUrl, Factory factory) {
        this(new RestxSpecLoader(), routerPath, serverId, baseUrl, factory);
    }

    public RestxSpecRunner(RestxSpecLoader specLoader, String routerPath, String serverId, String baseUrl, Factory factory) {
        this.specLoader = checkNotNull(specLoader);
        this.routerPath = checkNotNull(routerPath);
        this.serverId = checkNotNull(serverId);
        this.baseUrl = checkNotNull(baseUrl);
        givenSpecRules = newArrayList(transform(factory.queryByClass(GivenSpecRuleSupplier.class).findAsComponents(),
                Suppliers.<GivenSpecRule>supplierFunction()));
        givenRunners = factory.queryByClass(GivenRunner.class).findAsComponents();
        whenCheckers = factory.queryByClass(WhenChecker.class).findAsComponents();

        for (GivenSpecRule givenSpecRule : givenSpecRules) {
            givenSpecRule.onSetup(localMachines());
        }
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
        params.put(WhenHttpRequest.CONTEXT_NAME, serverId);
        params.put(WhenHttpRequest.BASE_URL, baseUrl + routerPath);

        runSpec(restxSpec, ImmutableMap.copyOf(params));
    }

    public void dispose() {
        for (GivenSpecRule givenSpecRule : givenSpecRules) {
            givenSpecRule.onTearDown(localMachines());
        }
    }


    private void runSpec(RestxSpec restxSpec, ImmutableMap<String, String> params) {
        List<GivenCleaner> givenCleaners = newArrayList();
        try {
            for (Given given : restxSpec.getGiven()) {
                Optional<GivenRunner> runnerFor = findRunnerFor(given);
                if (!runnerFor.isPresent()) {
                    throw new IllegalStateException(
                            "no runner found for given " + given + ". double check your classpath and factory settings.");
                }
                givenCleaners.add(runnerFor.get().run(given, params));
            }

            for (When when : restxSpec.getWhens()) {
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

    private Optional<WhenChecker> findCheckerFor(When when) {
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

    private Optional<GivenRunner> findRunnerFor(Given given) {
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


    protected Factory.LocalMachines localMachines() {
        return contextLocal(serverId);
    }

}
