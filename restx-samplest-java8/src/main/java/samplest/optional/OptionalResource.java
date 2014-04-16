package samplest.optional;

import restx.annotations.GET;
import restx.annotations.RestxResource;
import restx.factory.Component;

import java.util.Optional;

@RestxResource
@Component
public class OptionalResource {

    @GET("/optional/hasContent")
    public Optional<String> hasContent() {
        return Optional.of("hello");
    }

    @GET("/optional/isEmpty")
    public Optional<String> isEmpty() {
        return Optional.empty();
    }

    @GET("/optional/optionalParam")
    public Optional<String> optionalParam(Optional<String> param) {
        return param;
    }



}
