package samplest.core;

import restx.annotations.ExpiresAfter;
import restx.annotations.GET;
import restx.annotations.RestxResource;
import restx.factory.Component;

@RestxResource("/headers") @Component
public class HeadersResource {

    @GET("/expires")
    @ExpiresAfter("2d 4h")
    public String expireHeader() {
        return "hello";
    }

}
