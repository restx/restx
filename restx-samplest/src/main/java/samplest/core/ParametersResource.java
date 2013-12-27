package samplest.core;

import restx.annotations.GET;
import restx.annotations.RestxResource;
import restx.factory.Component;

/**
 * Date: 13/12/13
 * Time: 23:56
 */
@RestxResource @Component
public class ParametersResource {
    @GET("/params/path/{a}/:b/{c:\\d+}:d")
    public String pathparams(String a, String b, String c, String d) {
        return "a=" + a + " b=" + b + " c=" + c + " d=" + d;
    }
}
