package restx;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import restx.entity.MatchedEntityRoute;
import restx.jackson.JsonEntityRouteBuilder;
import restx.jackson.StdJsonProducerEntityRoute;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: xavierhanin
 * Date: 1/19/13
 * Time: 12:19 AM
 */
public class RestxRouter {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String groupName = "default";
        private String name = "default";
        private ObjectWriter writer;
        private ObjectReader reader;
        private List<RestxRoute> routes = Lists.newArrayList();

        public Builder name(final String name) {
            this.name = name;
            return this;
        }

        public Builder groupName(final String groupName) {
            this.groupName = groupName;
            return this;
        }

        public Builder withMapper(ObjectMapper mapper) {
            if (writer == null) {
                writer = mapper.writer();
            }
            if (reader == null) {
                reader = mapper.reader();
            }
            return this;
        }

        public Builder withObjectWriter(ObjectWriter writer) {
            this.writer = writer;
            return this;
        }

        public Builder withObjectReader(ObjectReader reader) {
            this.reader = reader;
            return this;
        }

        public Builder addRoute(RestxRoute route) {
            routes.add(route);
            return this;
        }

        public <O> Builder GET(String path, Class<O> outputType, final MatchedEntityRoute<Void, O> route) {
            return addRoute("GET", path, outputType, route);
        }

        public <O> Builder DELETE(String path, Class<O> outputType, final MatchedEntityRoute<Void, O> route) {
            return addRoute("DELETE", path, outputType, route);
        }

        public <O> Builder HEAD(String path, Class<O> outputType, final MatchedEntityRoute<Void, O> route) {
            return addRoute("HEAD", path, outputType, route);
        }

        public <O> Builder OPTIONS(String path, Class<O> outputType, final MatchedEntityRoute<Void, O> route) {
            return addRoute("OPTIONS", path, outputType, route);
        }

        public <O> Builder addRoute(String method, String path, Class<O> outputType, final MatchedEntityRoute<Void, O> route) {
            return addRoute(path, new StdRestxRequestMatcher(method, path), outputType, route);
        }

        public <O> Builder addRoute(String name, RestxRequestMatcher matcher, Class<O> outputType, final MatchedEntityRoute<Void, O> route) {
            routes.add(new StdJsonProducerEntityRoute<O>(name, outputType, writer.withType(outputType), matcher) {
                @Override
                protected Optional<O> doRoute(RestxRequest restxRequest, RestxResponse restxResponse, RestxRequestMatch match, Void i) throws IOException {
                    return route.route(restxRequest, match, i);
                }
            });
            return this;
        }

        public <I,O> Builder PUT(String path, Class<I> inputType, Class<O> outputType, MatchedEntityRoute<I, O> route) {
            return addRoute("PUT", path, inputType, outputType, route);
        }

        public <I,O> Builder POST(String path, Class<I> inputType, Class<O> outputType, MatchedEntityRoute<I, O> route) {
            return addRoute("POST", path, inputType, outputType, route);
        }

        public <I,O> Builder addRoute(String method, String path, Class<I> inputType, Class<O> outputType, MatchedEntityRoute<I, O> route) {
            return addRoute(path, new StdRestxRequestMatcher(method, path), inputType, outputType, route);
        }

        public <I, O> Builder addRoute(String name, StdRestxRequestMatcher matcher,
                                       Class<I> inputType, Class<O> outputType, MatchedEntityRoute<I, O> route) {
            routes.add(new JsonEntityRouteBuilder<I, O>()
                    .withObjectWriter(outputType, writer)
                    .withObjectReader(inputType, reader.withType(inputType))
                    .name(name)
                    .matcher(matcher)
                    .matchedEntityRoute(route)
                    .build()
            );
            return this;
        }

        public RestxRouter build() {
            return new RestxRouter(groupName, name, ImmutableList.copyOf(routes));
        }
    }

    private final ImmutableList<RestxRoute> routes;
    private final String name;
    private final String groupName;

    public RestxRouter(String name, RestxRoute... routes) {
        this("default", name, routes);
    }

    public RestxRouter(String groupName, String name, RestxRoute... routes) {
        this(groupName, name, ImmutableList.copyOf(routes));
    }

    public RestxRouter(String groupName, String name, ImmutableList<RestxRoute> routes) {
        this.groupName = checkNotNull(groupName);
        this.name = checkNotNull(name);
        this.routes = checkNotNull(routes);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        toString(sb, "");
        return sb.toString();
    }

    public void toString(StringBuilder sb, String indent) {
        sb.append(indent).append(name).append("[RestxRouter] {\n");
        for (RestxRoute route : routes) {
            sb.append(indent).append("    ").append(route).append("\n");
        }
        sb.append(indent).append("}");
    }

    public int getNbRoutes() {
        return routes.size();
    }

    public String getGroupName() {
        return groupName;
    }

    public ImmutableList<RestxRoute> getRoutes() {
        return routes;
    }

    public String getName() {
        return name;
    }
}
