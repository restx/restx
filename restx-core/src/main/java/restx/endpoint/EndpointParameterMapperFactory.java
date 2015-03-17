package restx.endpoint;

import com.google.common.base.Optional;

public interface EndpointParameterMapperFactory {
    Optional<? extends EndpointParameterMapper> getEndpointParameterMapperFor(EndpointParamDef endpointParamDef);
}