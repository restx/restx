package restx.specs;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.collect.*;
import com.google.common.io.InputSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.*;
import restx.common.MoreResources;
import restx.factory.Component;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * User: xavierhanin
 * Date: 4/8/13
 * Time: 12:40 PM
 */
@Component
public class RestxSpecRepository {
    private final Logger logger = LoggerFactory.getLogger(RestxSpecRepository.class);

    private ImmutableMap<String, RestxSpec> allSpecs;
    private RestxSpecLoader specLoader;

    public RestxSpecRepository() {
        this(new RestxSpecLoader());
    }

    public RestxSpecRepository(RestxSpecLoader specLoader) {
        this.specLoader = specLoader;
    }


    public Iterable<String> findAll() {
        return findAllSpecs().keySet();
    }

    public Optional<RestxSpec> findSpecById(String id) {
        return Optional.fromNullable(findAllSpecs().get(id));
    }

    public Iterable<String> findSpecsByOperation(String httpMethod, String path) {
        return filterSpecsByOperation(findAllSpecs(), httpMethod, path);
    }

    public Iterable<WhenHttpRequest> findSpecsByRequest(RestxRequest request) {
        return findWhensMatchingRequest(findAllSpecs(), request);
    }

    synchronized ImmutableMap<String, RestxSpec> findAllSpecs() {
        if (allSpecs == null) {
            allSpecs = ImmutableMap.copyOf(buildSpecsMap(false));
        }
        return allSpecs;
    }

    protected Map<String, RestxSpec> buildSpecsMap(boolean searchInSources) {
        Map<String, RestxSpec> specsMap = Maps.newLinkedHashMap();
        Map<String, URL> specs = MoreResources.findResources("", Pattern.compile(".*\\.spec\\.yaml"), searchInSources);
        for (final Map.Entry<String, URL> spec : specs.entrySet()) {
            try {
                specsMap.put(spec.getKey(), specLoader.load(spec.getKey(), new InputSupplier<InputStreamReader>() {
                    @Override
                    public InputStreamReader getInput() throws IOException {
                        return new InputStreamReader(spec.getValue().openStream(), Charsets.UTF_8);
                    }
                }));
            } catch (Exception e) {
                logger.warn("exception while loading restx spec " + spec + ": " + e, e);
            }
        }
        return specsMap;
    }

    Iterable<String> filterSpecsByOperation(ImmutableMap<String, RestxSpec> allSpecs,
                                                    String httpMethod, String path) {
        StdRouteMatcher matcher = new StdRouteMatcher(httpMethod, path);
        Collection<String> specs = Lists.newArrayList();
        for (Map.Entry<String, RestxSpec> spec : allSpecs.entrySet()) {
            for (When when : spec.getValue().getWhens()) {
                if (when instanceof WhenHttpRequest) {
                    WhenHttpRequest request = (WhenHttpRequest) when;
                    String requestPath = request.getPath();
                    if (!requestPath.startsWith("/")) {
                        requestPath = "/" + requestPath;
                    }
                    if (requestPath.indexOf("?") != -1) {
                        requestPath = requestPath.substring(0, requestPath.indexOf("?"));
                    }
                    Optional<RestxRouteMatch> match = matcher.match(HANDLER, request.getMethod(), requestPath);
                    if (match.isPresent()) {
                        specs.add(spec.getKey());
                        break;
                    }
                }
            }
        }
        return specs;
    }

    Iterable<WhenHttpRequest> findWhensMatchingRequest(ImmutableMap<String, RestxSpec> allSpecs, RestxRequest restxRequest) {
        Collection<WhenHttpRequest> matchingRequestsSpecs = Lists.newArrayList();
        for (Map.Entry<String, RestxSpec> spec : allSpecs.entrySet()) {
            for (When when : spec.getValue().getWhens()) {
                if (when instanceof WhenHttpRequest) {
                    WhenHttpRequest request = (WhenHttpRequest) when;
                    String requestPath = request.getPath();
                    if (!requestPath.startsWith("/")) {
                        requestPath = "/" + requestPath;
                    }
                    StdRequest stdRequest = StdRequest.builder()
                            .setBaseUri("http://restx.io") // baseUri is required but we won't use it
                            .setHttpMethod(request.getMethod()).setFullPath(requestPath).build();

                    if (restxRequest.getHttpMethod().equals(stdRequest.getHttpMethod())
                            && restxRequest.getRestxPath().equals(stdRequest.getRestxPath())) {
                        MapDifference<String, ImmutableList<String>> difference =
                                Maps.difference(restxRequest.getQueryParams(), stdRequest.getQueryParams());
                        if (difference.entriesOnlyOnRight().isEmpty()
                                && difference.entriesDiffering().isEmpty()) {
                            matchingRequestsSpecs.add(request);
                            break;
                        }
                    }
                }
            }
        }
        return matchingRequestsSpecs;
    }

    private static final RestxHandler HANDLER = new RestxHandler() {
        @Override
        public Optional<RestxRouteMatch> match(RestxRequest req) {
            return Optional.absent();
        }

        @Override
        public void handle(RestxRouteMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
        }
    };


}
