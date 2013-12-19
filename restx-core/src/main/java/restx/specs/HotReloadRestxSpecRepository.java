package restx.specs;

import com.google.common.collect.ImmutableMap;
import restx.factory.Alternative;

public class HotReloadRestxSpecRepository extends RestxSpecRepository {
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
        public DevRestxSpecRepository(RestxSpecLoader specLoader) {
            super(specLoader);
        }
    }

    @Alternative(to = RestxSpecRepository.class)
    @restx.factory.When(name="restx.mode", value="test")
    public static class TestRestxSpecRepository extends HotReloadRestxSpecRepository {
        public TestRestxSpecRepository(RestxSpecLoader specLoader) {
            super(specLoader);
        }
    }

    @Alternative(to = RestxSpecRepository.class)
    @restx.factory.When(name="restx.mode", value="infinirest")
    public static class InfinirestRestxSpecRepository extends HotReloadRestxSpecRepository {
        public InfinirestRestxSpecRepository(RestxSpecLoader specLoader) {
            super(specLoader);
        }
    }
}
