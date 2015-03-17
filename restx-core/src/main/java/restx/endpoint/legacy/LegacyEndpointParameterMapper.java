package restx.endpoint.legacy;

import com.google.common.base.Optional;
import restx.RestxRequest;
import restx.RestxRequestMatch;
import restx.common.Types;
import restx.converters.MainStringConverter;
import restx.endpoint.EndpointParamDef;
import restx.endpoint.EndpointParameterKind;
import restx.endpoint.EndpointParameterMapper;
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

        String underlyingType = Types.getTypeCanonicalNameFor(endpointParamDef.getType());
        Optional<String> queryParamStrValue = parameterKind.extractQueryParamStringedValueFor(endpointParamDef, request, match);

        if(String.class.getCanonicalName().equals(underlyingType)) {
            return (T) queryParamStrValue.orNull();
        } else {
            if(queryParamStrValue.isPresent()) {
                // Wondering if we shouldn't prepare the potentially costy Types.getRawType() call here
                return (T) converter.convert(queryParamStrValue.get(), Types.getRawType(endpointParamDef.getType()));
            } else {
                return null;
            }
        }
    }
}
