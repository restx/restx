package restx;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import restx.jackson.Views;

import java.io.IOException;
import java.io.PrintWriter;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: xavierhanin
 * Date: 1/19/13
 * Time: 8:10 AM
 */
public abstract class StdEntityRoute extends StdRoute {
    protected final ObjectMapper mapper;
    protected final HttpStatus successStatus;

    public StdEntityRoute(String name, ObjectMapper mapper, RestxRouteMatcher matcher) {
        this(name, mapper, matcher, HttpStatus.OK);
    }

    public StdEntityRoute(String name, ObjectMapper mapper, RestxRouteMatcher matcher, HttpStatus successStatus) {
        super(name, matcher);
        this.mapper = checkNotNull(mapper);
        this.successStatus = successStatus;
    }

    @Override
    public void handle(RestxRouteMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
        ctx.getLifecycleListener().onRouteMatch(this);
        Optional<?> result = doRoute(req, match);
        if (result.isPresent()) {
            resp.setStatus(successStatus.getCode());
            resp.setContentType("application/json");
            Object value = result.get();
            if (value instanceof Iterable) {
                value = Lists.newArrayList((Iterable) value);
            }
            ctx.getLifecycleListener().onBeforeWriteContent(this);
            writeValue(mapper, resp.getWriter(), value);
        } else {
            notFound(match,resp);
        }
    }

    protected void writeValue(ObjectMapper mapper, PrintWriter writer, Object value) throws IOException {
        getObjectWriter(mapper).writeValue(writer, value);
    }

    protected ObjectWriter getObjectWriter(ObjectMapper mapper) {
        return mapper.writerWithView(Views.Transient.class);
    }

    protected abstract Optional<?> doRoute(RestxRequest restxRequest, RestxRouteMatch match) throws IOException;
}
