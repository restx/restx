package samplest.core;

import restx.annotations.GET;
import restx.annotations.RestxResource;
import restx.factory.Component;

import javax.inject.Named;

/**
 * Date: 16/12/13
 * Time: 18:38
 */
@RestxResource
@Component
public class ProvidesWithExceptionResource {
    private final String msg;

    public ProvidesWithExceptionResource(@Named("providesWithExceptions") String msg) {
        this.msg = msg;
    }

    @GET("/providesWithExceptions")
    public String providesWithExceptions() {
        return msg;
    }
}
