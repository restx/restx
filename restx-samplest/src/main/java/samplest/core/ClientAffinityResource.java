package samplest.core;

import com.google.common.base.Optional;
import restx.annotations.GET;
import restx.annotations.RestxResource;
import restx.factory.Component;
import restx.security.PermitAll;

import javax.inject.Named;

/**
 * Date: 3/12/13
 * Time: 21:42
 */
@Component @RestxResource
public class ClientAffinityResource {
    public static final String COMPONENT_NAME = "clientAffinity";
    private final String clientAffinity;

    public ClientAffinityResource(@Named(COMPONENT_NAME) Optional<String> clientAffinity) {
        this.clientAffinity = clientAffinity.or("NONE");
    }

    @PermitAll @GET("/clientAffinity")
    public String getClientAffinity() {
        return clientAffinity;
    }
}
