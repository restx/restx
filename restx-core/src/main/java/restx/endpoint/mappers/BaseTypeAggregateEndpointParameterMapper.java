package restx.endpoint.mappers;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import restx.RestxRequest;
import restx.RestxRequestMatch;
import restx.common.Types;
import restx.types.AggregateType;
import restx.endpoint.EndpointParamDef;
import restx.endpoint.EndpointParameterKind;
import restx.factory.Component;

import java.util.List;

/**
 * @author fcamblor
 */
@Component
public class BaseTypeAggregateEndpointParameterMapper implements EndpointParameterMapper {

    private BaseTypeEndpointParameterMapper baseTypeEndpointParameterMapper;

    public BaseTypeAggregateEndpointParameterMapper(BaseTypeEndpointParameterMapper baseTypeEndpointParameterMapper) {
        this.baseTypeEndpointParameterMapper = baseTypeEndpointParameterMapper;
    }

    @Override
    public <T> T mapRequest(
            EndpointParamDef endpointParamDef,
            RestxRequest request,
            RestxRequestMatch match, EndpointParameterKind parameterKind) {

        List<String> values = parameterKind.extractQueryParamStringedValuesFor(endpointParamDef, request, match);
        if(values == null) {
            return null;
        }

        final Optional<AggregateType> aggregateType = Types.aggregateTypeFrom(endpointParamDef.getRawType().getCanonicalName());
        if(!aggregateType.isPresent()) {
            throw new IllegalStateException("Called mapRequest() on base type aggregate whereas it is not considered as an aggregate !");
        }

        final Class aggregatedType = Types.aggregatedTypeOf(endpointParamDef.getType());
        List convertedValues = FluentIterable.from(values).transform(new Function<String, Object>() {
            @Override
            public Object apply(String requestValue) {
                return baseTypeEndpointParameterMapper.convertRequestParamValue(Optional.fromNullable(requestValue), aggregatedType, aggregatedType);
            }
        }).toList();

        return (T) aggregateType.get().createFrom(convertedValues, aggregatedType);
    }

    public boolean isBaseTypeAggregateParam(EndpointParamDef endpointParamDef) {
        return Types.isAggregateType(endpointParamDef.getRawType().getCanonicalName());
    }
}
