package restx.endpoint.mappers;

import com.google.common.base.Optional;
import restx.endpoint.EndpointParamDef;

public interface EndpointParameterMapperFactory {
    Optional<? extends EndpointParameterMapper> getEndpointParameterMapperFor(EndpointParamDef endpointParamDef);
}