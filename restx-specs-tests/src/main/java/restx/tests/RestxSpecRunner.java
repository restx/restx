package restx.tests;

import com.google.common.base.Optional;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import restx.factory.Factory;
import restx.specs.Given;
import restx.specs.RestxSpec;
import restx.specs.RestxSpecLoader;
import restx.specs.When;
import restx.specs.WhenHttpRequest;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static restx.RestxMainRouterFactory.Blade;
import static restx.factory.Factory.LocalMachines.contextLocal;
import static restx.factory.Factory.LocalMachines.overrideComponents;

/**
 * User: xavierhanin
 * Date: 3/17/13
 * Time: 1:58 PM
 */
public class RestxSpecRunner {
    private final String serverId;
    private final String baseUrl;

    private final String routerPath;
    private final RestxSpecLoader specLoader;

    private final Iterable<GivenSpecRuleSupplier> givenSpecRuleSuppliers;
    private final Iterable<GivenRunner> givenRunners;
    private final Iterable<WhenChecker> whenCheckers;

    private final Map<String, Iterable<GivenSpecRule>> givenRulesPerBlade = new LinkedHashMap<>();


    public RestxSpecRunner(RestxSpecLoader specLoader, String routerPath, String serverId, String baseUrl, Factory factory) {
        this.specLoader = checkNotNull(specLoader);
        this.routerPath = checkNotNull(routerPath);
        this.serverId = checkNotNull(serverId);
        this.baseUrl = checkNotNull(baseUrl);
        givenSpecRuleSuppliers = factory.queryByClass(GivenSpecRuleSupplier.class).findAsComponents();
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
        for (GivenSpecRule givenSpecRule : getSpecRulesForCurrentBlade()) {
            params.putAll(givenSpecRule.getRunParams());
        }
        params.put(WhenHttpRequest.CONTEXT_NAME, serverId);
        params.put(WhenHttpRequest.BASE_URL, baseUrl + routerPath);

        runSpec(restxSpec, ImmutableMap.copyOf(params));
    }

    private synchronized Iterable<GivenSpecRule> getSpecRulesForCurrentBlade() {
        String currentBlade = Blade.current();
        Iterable<GivenSpecRule> givenSpecRules = givenRulesPerBlade.get(currentBlade);
        if (givenSpecRules == null) {
            givenSpecRules = newArrayList(transform(givenSpecRuleSuppliers,
                    Suppliers.<GivenSpecRule>supplierFunction()));
            for (GivenSpecRule givenSpecRule : givenSpecRules) {
                givenSpecRule.onSetup(contextLocal(bladeContextId(currentBlade)));
            }

            givenRulesPerBlade.put(currentBlade, givenSpecRules);
        }
        return givenSpecRules;
    }

    public synchronized void dispose() {
        for (Map.Entry<String, Iterable<GivenSpecRule>> blade : givenRulesPerBlade.entrySet()) {
            for (GivenSpecRule givenSpecRule : blade.getValue()) {
                givenSpecRule.onTearDown(contextLocal(bladeContextId(blade.getKey())));
            }
        }
    }

    private void runSpec(RestxSpec restxSpec, ImmutableMap<String, String> params) {
        List<GivenCleaner> givenCleaners = newArrayList();
        try {
            for (Given given : restxSpec.getGiven()) {
                Optional<GivenRunner<Given>> runnerFor = findRunnerFor(given);
                if (!runnerFor.isPresent()) {
                    throw new IllegalStateException(
                            "no runner found for given " + given + ". double check your classpath and factory settings.");
                }
                givenCleaners.add(runnerFor.get().run(given, params));
            }

            for (When when : restxSpec.getWhens()) {
                Optional<WhenChecker<When>> checkerFor = findCheckerFor(when);
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
            overrideComponents().clear();
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends When> Optional<WhenChecker<T>> findCheckerFor(T when) {
        if (when instanceof WhenChecker) {
            return Optional.of((WhenChecker<T>) when);
        }
        for (WhenChecker whenChecker : whenCheckers) {
            if (whenChecker.getWhenClass().isAssignableFrom(when.getClass())) {
                return Optional.of((WhenChecker<T>) whenChecker);
            }
        }

        return Optional.absent();
    }

    @SuppressWarnings("unchecked")
    private <T extends Given> Optional<GivenRunner<T>> findRunnerFor(T given) {
        if (given instanceof GivenRunner) {
            return Optional.of((GivenRunner<T>) given);
        }
        for (GivenRunner<?> givenRunner : givenRunners) {
            if (givenRunner.getGivenClass().isAssignableFrom(given.getClass())) {
                return Optional.of((GivenRunner<T>) givenRunner);
            }
        }

        return Optional.absent();
    }

    protected Factory.LocalMachines bladeLocalMachines() {
        return Blade.bladeLocalMachines(serverId);
    }

    private String bladeContextId() {
        return bladeContextId(Blade.current());
    }

    private String bladeContextId(String bladeId) {
        return Blade.contextId(serverId, bladeId);
    }
}
