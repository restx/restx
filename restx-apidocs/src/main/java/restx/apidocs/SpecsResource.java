package restx.apidocs;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.*;
import restx.annotations.GET;
import restx.annotations.RestxResource;
import restx.factory.Component;
import restx.specs.RestxSpec;
import restx.specs.RestxSpecLoader;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * User: xavierhanin
 * Date: 4/2/13
 * Time: 9:09 PM
 */
@Component @RestxResource(group = "admin")
public class SpecsResource {
    private final Logger logger = LoggerFactory.getLogger(SpecsResource.class);

    private ImmutableMap<String, RestxSpec> allSpecs;
    private RestxSpecLoader specLoader = new RestxSpecLoader();
    public SpecsResource() {
    }

    @GET("/@/specs")
    public Iterable<String> findSpecsForOperation(String httpMethod, String path) {
        ImmutableMap<String, RestxSpec> allSpecs = findAllSpecs();
        return filterSpecsByOperation(allSpecs, httpMethod, path);
    }

    @GET("/@/specs/{id}")
    public Optional<RestxSpec> getSpecById(String id) {
        try {
            return Optional.fromNullable(findAllSpecs().get(URLDecoder.decode(id, Charsets.UTF_8.name())));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    synchronized ImmutableMap<String, RestxSpec> findAllSpecs() {
        if (allSpecs == null) {
            Map<String, RestxSpec> specsMap = Maps.newLinkedHashMap();
            Set<String> specs = new ConfigurationBuilder()
                    .setUrls(ClasspathHelper.forPackage(""))
                    .setScanners(new ResourcesScanner())
                    .build()
                    .getResources(Pattern.compile(".*\\.spec\\.yaml"));
            for (String spec : specs) {
                try {
                    specsMap.put(spec, specLoader.load(spec));
                } catch (IOException e) {
                    logger.warn("io exception while loading restx spec " + spec + ": " + e, e);
                }
            }
            allSpecs = ImmutableMap.copyOf(specsMap);
        }
        return allSpecs;
    }

    Iterable<String> filterSpecsByOperation(ImmutableMap<String, RestxSpec> allSpecs,
                                                    String httpMethod, String path) {
        StdRouteMatcher matcher = new StdRouteMatcher(httpMethod, path);
        Collection<String> specs = Lists.newArrayList();
        for (Map.Entry<String, RestxSpec> spec : allSpecs.entrySet()) {
            for (RestxSpec.When when : spec.getValue().getWhens()) {
                if (when instanceof RestxSpec.WhenHttpRequest) {
                    RestxSpec.WhenHttpRequest request = (RestxSpec.WhenHttpRequest) when;
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
