package restx.endpoint.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import restx.RestxRequest;
import restx.RestxRequestMatch;
import restx.endpoint.EndpointParamDef;
import restx.endpoint.EndpointParameterKind;
import restx.factory.Component;
import restx.factory.ParamDef;
import restx.jackson.FrontObjectMapperFactory;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * @author fcamblor
 */
@Component
public class ComplexTypeEndpointParameterMapper implements EndpointParameterMapper {

    final ObjectMapper converter;

    public ComplexTypeEndpointParameterMapper(@Named(FrontObjectMapperFactory.MAPPER_NAME) ObjectMapper converter) {
        this.converter = converter;
    }

    @Override
    public <T> T mapRequest(
            EndpointParamDef endpointParamDef,
            RestxRequest request,
            RestxRequestMatch match, EndpointParameterKind parameterKind) {

        return (T) converter.convertValue(buildHierarchicalMapFrom(request.getQueryParams(), endpointParamDef.getParamValuesHandlings()), endpointParamDef.getRawType());
    }

    /**
     * Creates a "hierarchical map" from request parameters; this map should be easily deserializable to a target class by jackson afterwards
     * Also, note that this method will take a ParamValuesHandlings input to know if a given hierarchical map node should be
     * considered as an iterable (array) of values or not
     *
     * For instance :
     * - queryParams = { "foo": ["baz"], "foo2.bar2": ["baz2"], "foo3.bar3": ["baz3"] }
     * - paramValuesHandlings = { "foo3.bar3": ARRAY }
     *
     * will be transformed into following hierarchical map :
     * {
     *     "foo": "baz",
     *     "foo2": { "bar2": "baz2" },
     *     "foo3": { "bar3": ["baz3"] }
     * }
     */
    private static Map<String, Object> buildHierarchicalMapFrom(ImmutableMap<String, ImmutableList<String>> queryParams, ParamDef.ParamValuesHandlings paramValuesHandlings) {
        Map<String, Object> hierarchicalMap = new HashMap<>();

        for(String paramFullPath: queryParams.keySet()) {
            Map<String, Object> currentNode = hierarchicalMap;
            StringBuilder currentPathSB = new StringBuilder("");
            for(String pathChunk: paramFullPath.split("\\.")) {
                currentPathSB.append(pathChunk);
                String currentPath = currentPathSB.toString();
                // Leaf
                if(paramFullPath.equals(currentPath)) {
                    paramValuesHandlings.fillNode(currentNode, pathChunk, queryParams.get(currentPath), currentPath);
                // Not a leaf
                } else {
                    if(!currentNode.containsKey(currentPath)) {
                        currentNode.put(currentPath, new HashMap<String, Object>());
                    }
                    currentNode = (Map<String, Object>) currentNode.get(currentPath);
                }
                currentPathSB.append(".");
            }
        }

        // return null if map is empty as there is nothing to pass to jackson
        if (hierarchicalMap.isEmpty()) {
            return null;
        }

        return hierarchicalMap;
    }
}
