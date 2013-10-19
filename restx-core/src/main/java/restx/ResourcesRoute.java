package restx;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteStreams;
import com.google.common.io.Resources;
import restx.common.MoreResources;
import restx.http.HTTP;
import restx.http.HttpStatus;

import java.io.IOException;
import java.net.URL;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Resources route allows to serves files from the classpath.
 * <p>
 *     Example:
 *     <pre>
 *         new ResourcesRoute("myResources", "/")
 *     </pre>
 * </p>
 */
public class ResourcesRoute implements RestxRoute {
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

    public ResourcesRoute(String name, String baseRestPath, String baseResourcePath) {
        this(name, baseRestPath, baseResourcePath, ImmutableMap.<String, String>of());
    }

    public ResourcesRoute(String name, String baseRestPath, String baseResourcePath, ImmutableMap<String, String> aliases) {
        this.name = checkNotNull(name);
        this.baseRestPath = "/" + checkNotNull(baseRestPath).replaceAll("^/", "").replaceAll("/$", "") + "/";
        this.baseResourcePath = checkNotNull(baseResourcePath)
                .replace('.', '/').replaceAll("^/", "").replaceAll("/$", "") + "/";
        this.aliases = checkNotNull(aliases);
    }

    @Override
    public Optional<RestxRouteMatch> match(RestxRequest req) {
        if (req.getHttpMethod().equals("GET") && req.getRestxPath().startsWith(baseRestPath)) {
            return Optional.of(new RestxRouteMatch(this, baseRestPath + "*", req.getRestxPath()));
        } else {
            return Optional.absent();
        }
    }

    @Override
    public void handle(RestxRouteMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
        String relativePath = req.getRestxPath().substring(baseRestPath.length());
        relativePath = Optional.fromNullable(aliases.get(relativePath)).or(relativePath);
        try {
            URL resource = MoreResources.getResource(
                    baseResourcePath + relativePath, RestxContext.Modes.DEV.equals(ctx.getMode())
                                                    || RestxContext.Modes.TEST.equals(ctx.getMode()));
            resp.setLogLevel(RestxLogLevel.QUIET);
            resp.setStatus(HttpStatus.OK);
            resp.setContentType(HTTP.getContentTypeFromExtension(relativePath).or("application/octet-stream"));
            ByteStreams.copy(Resources.newInputStreamSupplier(resource), resp.getOutputStream());
        } catch (IllegalArgumentException e) {
            notFound(resp, relativePath);
        }
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
