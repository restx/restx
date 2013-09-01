package restx.tests;

import com.google.common.collect.ImmutableMap;
import restx.specs.When;

public interface WhenChecker<T extends When> {
    public Class<T> getWhenClass();
    void check(T when, ImmutableMap<String, String> params);
}
