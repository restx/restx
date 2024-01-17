package samplest.factory;

import restx.annotations.GET;
import restx.annotations.RestxResource;
import restx.factory.Component;
import restx.security.PermitAll;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RestxResource("/factory")
public class FactoryInjectionResource {

    Set<TestSetInjection> testSetInjection;

    FactoryInjectionResource(Set<TestSetInjection> testSetInjection) {
        this.testSetInjection = testSetInjection;
    }

    @PermitAll
    @GET("/set")
    public Set<String> testSet() {
        return testSetInjection.stream()
                .map(value -> value.getClass().getName())
                .collect(Collectors.toSet());
    }

}

interface TestSetInjection {
}

@Component
class TestSetInjectionImpl01 implements TestSetInjection {
}

@Component
class TestSetInjectionImpl02 implements TestSetInjection {
}