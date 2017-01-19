package restx.endpoint.mappers;

import com.fasterxml.jackson.databind.type.SimpleType;
import com.google.common.base.Optional;
import restx.RestxRequest;
import restx.RestxRequestMatch;
import restx.converters.MainStringConverter;
import restx.endpoint.EndpointParamDef;
import restx.endpoint.EndpointParameterKind;
import restx.factory.Component;

import java.lang.reflect.Type;

/**
 * @author fcamblor
 */
@Component
public class BaseTypeEndpointParameterMapper implements EndpointParameterMapper {

    final MainStringConverter converter;

    public BaseTypeEndpointParameterMapper(MainStringConverter converter) {
        this.converter = converter;
    }

    @Override
    public <T> T mapRequest(
            EndpointParamDef endpointParamDef,
            RestxRequest request,
            RestxRequestMatch match, EndpointParameterKind parameterKind) {

        Optional<String> queryParamStrValue = parameterKind.extractQueryParamStringedValueFor(endpointParamDef, request, match);

        return convertRequestParamValue(queryParamStrValue, endpointParamDef.getType(), endpointParamDef.getRawType());
    }

    public <T> T convertRequestParamValue(Optional<String> queryParamStrValue, Type paramType, Class paramRawType) {
        if(String.class == paramType) {
            return (T) queryParamStrValue.orNull();
        } else {
            if(queryParamStrValue.isPresent()) {
                return (T) converter.convert(queryParamStrValue.get(), paramRawType);
            } else {
                return null;
            }
        }
    }

    public boolean isBaseTypeParam(EndpointParamDef endpointParamDef) {
        return converter.canDeserialize(SimpleType.construct(endpointParamDef.getRawType()));
    }
}
