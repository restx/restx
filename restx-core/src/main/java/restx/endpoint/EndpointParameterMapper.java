package restx.endpoint;

import restx.RestxRequest;
import restx.RestxRequestMatch;

public interface EndpointParameterMapper {
    <T> T mapRequest(EndpointParameter endpointParameter, RestxRequest request, RestxRequestMatch match, EndpointParameterKind parameterKind);
}