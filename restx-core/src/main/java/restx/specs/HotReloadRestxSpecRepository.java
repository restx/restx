package restx.specs;

import com.google.common.collect.ImmutableMap;
import restx.factory.Alternative;

public class HotReloadRestxSpecRepository extends RestxSpecRepository {
    public HotReloadRestxSpecRepository() {
    }

    public HotReloadRestxSpecRepository(RestxSpecLoader specLoader) {
        super(specLoader);
    }

    @Override
    synchronized ImmutableMap<String, RestxSpec> findAllSpecs() {
        return ImmutableMap.copyOf(buildSpecsMap(true));
    }

    @Alternative(to = RestxSpecRepository.class)
    @restx.factory.When(name="restx.mode", value="dev")
    public static class DevRestxSpecRepository extends HotReloadRestxSpecRepository {
    }

    @Alternative(to = RestxSpecRepository.class)
    @restx.factory.When(name="restx.mode", value="test")
    public static class TestRestxSpecRepository extends HotReloadRestxSpecRepository {
    }
}
