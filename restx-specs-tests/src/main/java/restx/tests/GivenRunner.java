package restx.tests;

import com.google.common.collect.ImmutableMap;
import restx.specs.Given;

public interface GivenRunner<T extends Given> {
    public Class<T> getGivenClass();
    public GivenCleaner run(T given, ImmutableMap<String, String> params);
}
