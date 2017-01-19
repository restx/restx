package restx.endpoint.mappers.legacy;

import com.google.common.base.Optional;
import restx.RestxRequest;
import restx.RestxRequestMatch;
import restx.converters.MainStringConverter;
import restx.endpoint.EndpointParamDef;
import restx.endpoint.EndpointParameterKind;
import restx.endpoint.mappers.EndpointParameterMapper;
import restx.factory.Component;

/**
 * @author fcamblor
 */
@Component
public class LegacyEndpointParameterMapper implements EndpointParameterMapper {

    final MainStringConverter converter;

    public LegacyEndpointParameterMapper(MainStringConverter converter) {
        this.converter = converter;
    }

    @Override
    public <T> T mapRequest(
            EndpointParamDef endpointParamDef,
            RestxRequest request,
            RestxRequestMatch match, EndpointParameterKind parameterKind) {

        Optional<String> queryParamStrValue = parameterKind.extractQueryParamStringedValueFor(endpointParamDef, request, match);

        if(String.class == endpointParamDef.getType()) {
            return (T) queryParamStrValue.orNull();
        } else {
            if(queryParamStrValue.isPresent()) {
                return (T) converter.convert(queryParamStrValue.get(), endpointParamDef.getRawType());
            } else {
                return null;
            }
        }
    }
}
