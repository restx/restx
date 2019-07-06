package restx.endpoint.mappers;

import com.google.common.base.Optional;
import restx.endpoint.EndpointParamDef;
import restx.factory.Component;
import restx.types.Types;

/**
 * @author fcamblor
 */
@Component
public class DefaultEndpointParameterMapperFactory implements EndpointParameterMapperFactory {

    private final BaseTypeAggregateEndpointParameterMapper baseTypeAggregateEndpointParameterMapper;
    private final BaseTypeEndpointParameterMapper baseTypeEndpointParameterMapper;
    private final ComplexTypeEndpointParameterMapper complexTypeEndpointParameterMapper;

    public DefaultEndpointParameterMapperFactory(
            BaseTypeAggregateEndpointParameterMapper baseTypeAggregateEndpointParameterMapper,
            BaseTypeEndpointParameterMapper baseTypeEndpointParameterMapper,
            ComplexTypeEndpointParameterMapper complexTypeEndpointParameterMapper
    ) {
        this.baseTypeAggregateEndpointParameterMapper = baseTypeAggregateEndpointParameterMapper;
        this.baseTypeEndpointParameterMapper = baseTypeEndpointParameterMapper;
        this.complexTypeEndpointParameterMapper = complexTypeEndpointParameterMapper;
    }

    @Override
    public Optional<? extends EndpointParameterMapper> getEndpointParameterMapperFor(EndpointParamDef endpointParamDef) {
        // First option : we're on an aggregate (eg Array | Iterable) of "base" types
        // => we should call BaseTypeAggregateEndpointParameterMapper
        if(Types.isAggregateType(endpointParamDef.getRawType().getCanonicalName())) {
            return Optional.of(baseTypeAggregateEndpointParameterMapper);
        // Second option : we're on a "base type" (eg a raw type, a Boxing type, or some type considered as "basic" such
        // as Date or joda dates)
        } else if(Types.isRawType(endpointParamDef.getType())) {
            return Optional.of(baseTypeEndpointParameterMapper);
        // Third option : we're on a "complex type", eg a POJO which should be filled with request params
        } else if(!Types.isRawType(endpointParamDef.getType())){
            return Optional.of(complexTypeEndpointParameterMapper);
        } else {
            return Optional.absent();
        }
    }
}
