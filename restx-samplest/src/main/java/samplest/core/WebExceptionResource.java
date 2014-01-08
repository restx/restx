package samplest.core;

import restx.RestxRequest;
import restx.RestxResponse;
import restx.WebException;
import restx.annotations.GET;
import restx.annotations.RestxResource;
import restx.factory.Component;
import restx.http.HttpStatus;

import java.io.IOException;

/**
 * Date: 8/1/14
 * Time: 16:38
 */
@RestxResource @Component
public class WebExceptionResource {
    @GET("/core/webexception/redirect")
    public void redirect() {
        throw new WebException(HttpStatus.FOUND) {
            @Override
            public void writeTo(RestxRequest restxRequest, RestxResponse restxResponse) throws IOException {
                restxResponse
                        .setStatus(getStatus())
                        .setHeader("Location", "/api/core/hello?who=restx");
            }
        };
    }
}
