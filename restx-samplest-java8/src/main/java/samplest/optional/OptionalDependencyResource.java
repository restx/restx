package samplest.optional;

import restx.annotations.GET;
import restx.annotations.RestxResource;
import restx.factory.Component;
import samplest.foobar.Bar;
import samplest.foobar.Foo;

import java.util.Optional;

@RestxResource
@Component
public class OptionalDependencyResource {

    final Optional<Foo> foo;
    final Optional<Bar> bar;

    public OptionalDependencyResource(Optional<Foo> foo, Optional<Bar> bar) {
        this.foo = foo;
        this.bar = bar;
    }

    @GET("/optional/dependency/foo")
    public Boolean isFooPresent() {
        return foo.isPresent();
    }

    @GET("/optional/dependency/bar")
    public Boolean isBarPresent() {
        return bar.isPresent();
    }
}
