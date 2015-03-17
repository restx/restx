package restx.endpoint;

import com.google.common.base.Optional;
import restx.RestxRequest;
import restx.RestxRequestMatch;

/**
 * Created by fcamblor on 06/02/15.
 */
public enum EndpointParameterKind {
    QUERY {
        @Override
        public Optional<String> extractQueryParamStringedValueFor(EndpointParamDef parameter, RestxRequest request, RestxRequestMatch match) {
            return request.getQueryParam(parameter.getName());
        }
    }, PATH {
        @Override
        public Optional<String> extractQueryParamStringedValueFor(EndpointParamDef parameter, RestxRequest request, RestxRequestMatch match) {
            return Optional.of(match.getPathParam(parameter.getName()));
        }
    };

    public abstract Optional<String> extractQueryParamStringedValueFor(EndpointParamDef parameter, RestxRequest request, RestxRequestMatch match);
}
