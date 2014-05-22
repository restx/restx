package samplest.etag;

import restx.annotations.GET;
import restx.annotations.RestxResource;
import restx.factory.Component;

/**
 * Date: 22/5/14
 * Time: 20:36
 */
@RestxResource
@Component
public class SampleETagResource {
    @GET("/etag/{name}")
    public ETagSampleObject get(String name) {
        return new ETagSampleObject(name);
    }
}
