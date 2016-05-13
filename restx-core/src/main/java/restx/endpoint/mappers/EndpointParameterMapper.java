package restx.endpoint.mappers;

import restx.RestxRequest;
import restx.RestxRequestMatch;
import restx.endpoint.EndpointParamDef;
import restx.endpoint.EndpointParameterKind;

public interface EndpointParameterMapper {
    <T> T mapRequest(EndpointParamDef endpointParamDef, RestxRequest request, RestxRequestMatch match, EndpointParameterKind parameterKind);
}