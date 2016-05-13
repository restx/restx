package restx.endpoint.mappers.legacy;

import com.google.common.base.Optional;
import restx.endpoint.EndpointParamDef;
import restx.endpoint.mappers.EndpointParameterMapper;
import restx.endpoint.mappers.EndpointParameterMapperFactory;
import restx.factory.Component;

/**
 * @author fcamblor
 */
@Component(priority = 1000)
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
