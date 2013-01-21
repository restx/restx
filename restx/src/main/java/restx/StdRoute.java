package restx;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
    public boolean route(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getRequestURI().substring((req.getContextPath() + req.getServletPath()).length());
        Optional<RestxRouteMatch> match = matcher.match(req.getMethod(), path);
        if (match.isPresent()) {
            Optional<?> result = doRoute(match.get());
            if (result.isPresent()) {
                resp.setStatus(200);
                resp.setContentType("application/json");
                mapper.writeValue(resp.getWriter(), result.get());
                resp.getWriter().close();
            } else {
                resp.setStatus(404);
            }
            return true;
        }
        return false;
    }

    protected abstract Optional<?> doRoute(RestxRouteMatch match);

    @Override
    public String toString() {
        return matcher.toString() + " => " + name;
    }
}
