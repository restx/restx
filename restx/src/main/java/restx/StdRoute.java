package restx;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: xavierhanin
 * Date: 1/19/13
 * Time: 8:10 AM
 */
public abstract class StdRoute implements RestxRoute {
    private final String name;
    private final RestxRouteMatcher matcher;
    private final ObjectMapper mapper;

    public StdRoute(String name, ObjectMapper mapper, RestxRouteMatcher matcher) {
        this.name = checkNotNull(name);
        this.mapper = checkNotNull(mapper);
        this.matcher = checkNotNull(matcher);
    }

    @Override
    public boolean route(HttpServletRequest req, HttpServletResponse resp, RouteLifecycleListener listener) throws IOException {
        String path = req.getRequestURI().substring((req.getContextPath() + req.getServletPath()).length());
        Optional<RestxRouteMatch> match = matcher.match(req.getMethod(), path);
        if (match.isPresent()) {
            listener.onRouteMatch(this);
            Optional<?> result = doRoute(new HttpServletRestxRequest(req), match.get());
            if (result.isPresent()) {
                resp.setStatus(200);
                resp.setContentType("application/json");
                Object value = result.get();
                if (value instanceof Iterable) {
                    value = Lists.newArrayList((Iterable) value);
                }
                listener.onBeforeWriteContent(this);
                writeValue(mapper, resp.getWriter(), value);
                resp.getWriter().close();
            } else {
                resp.setStatus(404);
            }
            return true;
        }
        return false;
    }

    protected void writeValue(ObjectMapper mapper, PrintWriter writer, Object value) throws IOException {
        mapper.writeValue(writer, value);
    }

    protected abstract Optional<?> doRoute(RestxRequest restxRequest, RestxRouteMatch match) throws IOException;

    @Override
    public String toString() {
        return matcher.toString() + " => " + name;
    }
}
