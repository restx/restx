package restx.factory;

import com.google.common.base.Optional;

import javax.inject.Named;

/**
 * @author fcamblor
 */
@Module
public class TestOptionalDependency {
    @Provides
    @Named("optional.dep.result1")
    public String optionalDepResult1(@Named("missing.dependency") Optional<String> missingDependency) {
        if(missingDependency.isPresent()){
            return "present:"+missingDependency.get();
        } else {
            return "absent";
        }
    }

    @Provides
    @Named("optional.dep.result2")
    public String optionalDepResult2(@Named Optional<Foo> missingDependency) {
        if(missingDependency.isPresent()){
            return "present:"+missingDependency.get().toString();
        } else {
            return "absent";
        }
    }

    public static class Foo{}
}
