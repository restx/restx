package restx.factory;

import com.google.common.base.Optional;

import javax.inject.Named;

/**
 * @author fcamblor
 */
@Module
public class TestMandatoryDependency {
    @Provides
    @Named("mandatory.dep.result1")
    public String mandatoryDepResult1(@Named("missing.dependency") String missingDependency) {
        return "present:"+missingDependency;
    }

    @Provides
    @Named("mandatory.dep.result2")
    public String mandatoryDepResult2(@Named Foo missingDependency) {
        return "present:"+missingDependency.toString();
    }

    public static class Foo{}
}
