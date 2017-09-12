package restx.http;

import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.samskivert.mustache.Template;
import restx.RestxRequest;
import restx.RestxResponse;
import restx.StdRoute;
import restx.annotations.LocationHeader;
import restx.common.Mustaches;
import restx.description.OperationDescription;
import restx.description.ResourceDescription;
import restx.factory.Component;

import java.io.StringWriter;
import java.util.concurrent.ExecutionException;

/**
 * Generates a "Location" HTTP header on endpoints having @LocationHeader annotation
 * URL passed to the annotation will be interpolated with :
 * - Object properties returned by the endpoint
 * - special "_currentUri_" targetting current restx uri used for current endpoint
 * - special "_baseUri_" targetting current base uri, including base API context
 *
 * Example usages :
 * <pre>
 * \@POST("/foos") @LocationHeader("{_currentUri_}/{id}") // Returns /api/foos/123456 when Foo.id is "123456"
 * \@POST("/foos") @LocationHeader("{_baseUri_}/foos/{id}") // Same as above
 * </pre>
 */
@Component
public class LocationHeaderFilter extends EntityRelatedFilter {

    private LoadingCache<String, Template> templatesCache = CacheBuilder.newBuilder()
            .maximumSize(200)
            .build(new CacheLoader<String, Template>() {
                @Override
                public Template load(String template) throws Exception {
                    return Mustaches.compileTemplateWithSingleBrackets(template);
                }
            });

    public LocationHeaderFilter() {
        super(Predicates.<StdRoute>alwaysTrue(), Predicates.<ResourceDescription>alwaysTrue(),
                new OperationDescription.Matcher().havingAnyAnnotations(LocationHeader.class)
        );
    }

    @Override
    protected void onEntityOutput(StdRoute stdRoute, RestxRequest req, RestxResponse resp,
                                  Optional<?> input, Optional<?> output,
                                  ResourceDescription resourceDescription, OperationDescription operationDescription) {

        if(!output.isPresent()) {
            return;
        }

        LocationHeader locationHeaderAnn = operationDescription.findAnnotation(LocationHeader.class).get();
        try {
            StringWriter locationWriter = new StringWriter();
            templatesCache.get(locationHeaderAnn.value()).execute(output.get(), ImmutableMap.of(
                    "_baseUri_", req.getBaseUri(),
                    "_currentUri_", req.getBaseUri()+req.getRestxUri()
            ), locationWriter);

            resp.setHeader("Location", locationWriter.toString());
        } catch (ExecutionException e) {
            Throwables.propagate(e);
        }
    }
}
