package restx.tests;

import com.google.common.collect.ImmutableMap;
import restx.specs.RestxSpec;

public interface WhenChecker<T extends RestxSpec.When> {
    public Class<T> getWhenClass();
    void check(T when, ImmutableMap<String, String> params);
}
