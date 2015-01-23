package restx;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteStreams;
import com.google.common.io.Resources;
import restx.common.MoreResources;
import restx.http.HTTP;
import restx.http.HttpStatus;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Resources route allows to serves files from the classpath.
 *
 * Example:
 * <pre>new ResourcesRoute("myResources", "/")</pre>
 */
public class ResourcesRoute implements RestxRoute, RestxHandler {
    /**
     * Resource name, for toString only.
     */
    private final String name;
    /**
     * Base restx path from which resources should be served.
     * Sanitized will always a leading and trailing slash.
     */
    private final String baseRestPath;
    /**
     * Base resource name path from which resources should be located in classpath.
     * Sanitized with no leading slash, dots replaced with slashes and always a trailing slash.
     */
    private final String baseResourcePath;
    private final ImmutableMap<String, String> aliases;
    private final ImmutableList<CachedResourcePolicy> cachedResourcePolicies;

    public static class ResourceInfo {
        final String contentType;
        final String path;

        public ResourceInfo(String contentType, String path) {
            this.contentType = contentType;
            this.path = path;
        }

        public String getContentType() {
            return contentType;
        }

        public String getPath() {
            return path;
        }
    }

    public static class CachedResourcePolicy {
        final Predicate<ResourceInfo> matcher;
        final String cacheValue;

        public CachedResourcePolicy(Predicate<ResourceInfo> matcher, String cacheValue) {
            this.matcher = matcher;
            this.cacheValue = cacheValue;
        }

        public boolean matches(String contentType, String path) {
            return matcher.apply(new ResourceInfo(contentType, path));
        }

        public String getCacheValue() {
            return cacheValue;
        }
    }


    public ResourcesRoute(String name, String baseRestPath, String baseResourcePath) {
        this(name, baseRestPath, baseResourcePath, ImmutableMap.<String, String>of());
    }

    public ResourcesRoute(String name, String baseRestPath, String baseResourcePath, ImmutableMap<String, String> aliases) {
        this(name, baseRestPath, baseResourcePath, aliases, Collections.<CachedResourcePolicy>emptyList());
    }

    public ResourcesRoute(String name, String baseRestPath, String baseResourcePath, ImmutableMap<String, String> aliases, List<CachedResourcePolicy> cachedResourcePolicies) {
        this.name = checkNotNull(name);
        this.baseRestPath = ("/" + checkNotNull(baseRestPath) + "/").replaceAll("/+", "/");
        this.baseResourcePath = checkNotNull(baseResourcePath)
                .replace('.', '/').replaceAll("^/", "").replaceAll("/$", "") + "/";
        this.aliases = checkNotNull(aliases);
        this.cachedResourcePolicies = ImmutableList.copyOf(cachedResourcePolicies);
    }

    @Override
    public Optional<RestxHandlerMatch> match(RestxRequest req) {
        if (req.getHttpMethod().equals("GET") && req.getRestxPath().startsWith(baseRestPath)) {
            return Optional.of(new RestxHandlerMatch(
                    new StdRestxRequestMatch(baseRestPath + "*", req.getRestxPath()),
                    this));
        } else {
            return Optional.absent();
        }
    }

    @Override
    public void handle(RestxRequestMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
        String relativePath = req.getRestxPath().substring(baseRestPath.length());
        relativePath = Optional.fromNullable(aliases.get(relativePath)).or(relativePath);
        try {
            URL resource = MoreResources.getResource(
                    baseResourcePath + relativePath, RestxContext.Modes.DEV.equals(ctx.getMode())
                                                    || RestxContext.Modes.TEST.equals(ctx.getMode())
                                                    || RestxContext.Modes.INFINIREST.equals(ctx.getMode())
            );
            resp.setLogLevel(RestxLogLevel.QUIET);
            resp.setStatus(HttpStatus.OK);
            String contentType = HTTP.getContentTypeFromExtension(relativePath).or("application/octet-stream");
            Optional<CachedResourcePolicy> cachedResourcePolicy = cachePolicyMatching(contentType, relativePath);
            if(cachedResourcePolicy.isPresent()) {
                resp.setHeader("Cache-Control", cachedResourcePolicy.get().getCacheValue());
            }
            resp.setContentType(contentType);
            Resources.asByteSource(resource).copyTo(resp.getOutputStream());
        } catch (IllegalArgumentException e) {
            notFound(resp, relativePath);
        }
    }

    private Optional<CachedResourcePolicy> cachePolicyMatching(String contentType, String path) {
        for(CachedResourcePolicy cachedResourcePolicy : cachedResourcePolicies){
            if(cachedResourcePolicy.matches(contentType, path)){
                return Optional.of(cachedResourcePolicy);
            }
        }
        return Optional.absent();
    }

    private void notFound(RestxResponse resp, String relativePath) throws IOException {
        resp.setStatus(HttpStatus.NOT_FOUND);
        resp.setContentType("text/plain");
        resp.getWriter().println("Resource route matched '" + this + "', but resource "
                + relativePath + " not found in " + baseResourcePath + ".");
    }

    public String getName() {
        return name;
    }

    public String getBaseRestPath() {
        return baseRestPath;
    }

    public String getBaseResourcePath() {
        return baseResourcePath;
    }

    @Override
    public String toString() {
        return "GET " + baseRestPath + "* => " + name;
    }
}
