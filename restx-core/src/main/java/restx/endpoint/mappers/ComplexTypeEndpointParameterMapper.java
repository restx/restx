package restx.endpoint.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import restx.RestxRequest;
import restx.RestxRequestMatch;
import restx.endpoint.EndpointParamDef;
import restx.endpoint.EndpointParameterKind;
import restx.factory.Component;

/**
 * @author fcamblor
 */
@Component
public class ComplexTypeEndpointParameterMapper implements EndpointParameterMapper {

    final ObjectMapper converter;

    public ComplexTypeEndpointParameterMapper(ObjectMapper converter) {
        this.converter = converter;
    }

    @Override
    public <T> T mapRequest(
            EndpointParamDef endpointParamDef,
            RestxRequest request,
            RestxRequestMatch match, EndpointParameterKind parameterKind) {

        throw new IllegalArgumentException("Complex type deserialization on query parameters is not supported yet");
    }

    public boolean isComplexTypeParam(EndpointParamDef endpointParamDef) {
        // Everything is a complex type
        return true;
    }
}
