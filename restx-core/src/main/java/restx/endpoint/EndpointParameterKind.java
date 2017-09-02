package restx.endpoint;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import restx.RestxRequest;
import restx.RestxRequestMatch;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by fcamblor on 06/02/15.
 */
public enum EndpointParameterKind {
    QUERY {
        @Override
        public Optional<String> extractQueryParamStringedValueFor(EndpointParamDef parameter, RestxRequest request, RestxRequestMatch match) {
            return request.getQueryParam(parameter.getName());
        }
        @Override
        public List<String> extractQueryParamStringedValuesFor(EndpointParamDef parameter, RestxRequest request, RestxRequestMatch match) {
            return request.getQueryParams(parameter.getName());
        }
    }, PATH {
        @Override
        public Optional<String> extractQueryParamStringedValueFor(EndpointParamDef parameter, RestxRequest request, RestxRequestMatch match) {
            return Optional.of(match.getPathParam(parameter.getName()));
        }
        @Override
        public List<String> extractQueryParamStringedValuesFor(EndpointParamDef parameter, RestxRequest request, RestxRequestMatch match) {
            return extractQueryParamStringedValueFor(parameter, request, match).transform(new Function<String, List<String>>() {
                @Override
                public List<String> apply(String pathParam) {
                    return Lists.newArrayList(pathParam);
                }
            }).or(Collections.<String>emptyList());
        }
    }, HEADER {
        @Override
        public Optional<String> extractQueryParamStringedValueFor(EndpointParamDef parameter, RestxRequest request, RestxRequestMatch match) {
            return request.getHeader(parameter.getName());
        }
        @Override
        public List<String> extractQueryParamStringedValuesFor(EndpointParamDef parameter, RestxRequest request, RestxRequestMatch match) {
            return extractQueryParamStringedValueFor(parameter, request, match).transform(new Function<String, List<String>>() {
                @Override
                public List<String> apply(String headerParam) {
                    return Lists.newArrayList(headerParam);
                }
            }).or(Collections.<String>emptyList());
        }
    };

    public abstract Optional<String> extractQueryParamStringedValueFor(EndpointParamDef parameter, RestxRequest request, RestxRequestMatch match);
    public abstract List<String> extractQueryParamStringedValuesFor(EndpointParamDef parameter, RestxRequest request, RestxRequestMatch match);
}
