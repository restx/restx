package restx.endpoint.legacy;

import com.google.common.base.Optional;
import restx.endpoint.EndpointParamDef;
import restx.endpoint.EndpointParameterMapper;
import restx.endpoint.EndpointParameterMapperFactory;
import restx.factory.Component;

/**
 * @author fcamblor
 */
@Component
public class LegacyEndpointParameterMapperFactory implements EndpointParameterMapperFactory {

    private final LegacyEndpointParameterMapper legacyEndpointParameterMapper;

    public LegacyEndpointParameterMapperFactory(LegacyEndpointParameterMapper legacyEndpointParameterMapper) {
        this.legacyEndpointParameterMapper = legacyEndpointParameterMapper;
    }

    @Override
    public Optional<? extends EndpointParameterMapper> getEndpointParameterMapperFor(EndpointParamDef endpointParamDef) {
        return Optional.of(legacyEndpointParameterMapper);
    }
}
