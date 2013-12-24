package samplest.core;

import restx.annotations.GET;
import restx.annotations.RestxResource;
import restx.common.UUIDGenerator;
import restx.factory.Component;

/**
 * Date: 24/12/13
 * Time: 14:15
 */
@Component @RestxResource
public class UUIDsResource {
    private final UUIDGenerator uuidGenerator;

    public UUIDsResource(UUIDGenerator uuidGenerator) {
        this.uuidGenerator = uuidGenerator;
    }

    @GET("/uuids/random")
    public String genUUID() {
        return uuidGenerator.doGenerate();
    }
}
