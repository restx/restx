package restx.tests;

import com.google.common.collect.ImmutableMap;
import restx.specs.RestxSpec;

public interface GivenRunner<T extends RestxSpec.Given> {
    public Class<T> getGivenClass();
    public GivenCleaner run(T given, ImmutableMap<String, String> params);
}
