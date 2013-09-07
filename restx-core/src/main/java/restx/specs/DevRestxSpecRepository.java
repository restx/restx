package restx.specs;

import com.google.common.collect.ImmutableMap;
import restx.factory.Alternative;
import restx.factory.When;

@Alternative(to = RestxSpecRepository.class)
@When(name="restx.mode", value="dev")
public class DevRestxSpecRepository extends RestxSpecRepository {
    public DevRestxSpecRepository() {
    }

    public DevRestxSpecRepository(RestxSpecLoader specLoader) {
        super(specLoader);
    }

    @Override
    synchronized ImmutableMap<String, RestxSpec> findAllSpecs() {
        return ImmutableMap.copyOf(buildSpecsMap(true));
    }
}
