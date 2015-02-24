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
        public Optional<String> extractQueryParamStringedValueFor(EndpointParameter parameter, RestxRequest request, RestxRequestMatch match) {
            return request.getQueryParam(parameter.getParameter().getName());
        }
    }, PATH {
        @Override
        public Optional<String> extractQueryParamStringedValueFor(EndpointParameter parameter, RestxRequest request, RestxRequestMatch match) {
            return Optional.of(match.getPathParam(parameter.getParameter().getName()));
        }
    };

    public abstract Optional<String> extractQueryParamStringedValueFor(EndpointParameter parameter, RestxRequest request, RestxRequestMatch match);
}
