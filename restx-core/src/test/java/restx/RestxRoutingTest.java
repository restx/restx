package restx;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import restx.RestxRouting.Match;
import restx.factory.NamedComponent;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Date: 24/5/14
 * Time: 23:24
 */
public class RestxRoutingTest {

    @Test
    public void should_match_with_filters_in_order() throws Exception {
        RestxRouting routing = new RestxRouting(
                ImmutableList.of(
                        NamedComponent.of(RestxFilter.class, "F1", 0, new TestFilter("F1"))),
                ImmutableList.of(
                        NamedComponent.of(RestxRouteFilter.class, "RF1", -10, new TestRouteFilter("RF1")),
                        NamedComponent.of(RestxRouteFilter.class, "RF2", 10, new TestRouteFilter("RF2"))
                ),
                ImmutableList.<RestxRoute>of(ROUTE)
        );

        Optional<Match> m = routing.match(StdRequest.builder()
                .setHttpMethod("GET").setRestxPath("/test").setBaseUri("http://localhost/api").build());

        assertThat(m.isPresent()).isTrue();
        assertThat(m.get().getMatches()).extracting("handler.name")
                .containsExactly("RF1", "F1", "RF2", "ROUTE");
    }

    private class TestFilter implements RestxFilter, RestxHandler {
        private final String name;

        private TestFilter(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public Optional<RestxHandlerMatch> match(RestxRequest req) {
            return Optional.of(new RestxHandlerMatch(new StdRestxRequestMatch(req.getRestxPath()), this));
        }
        @Override
        public void handle(RestxRequestMatch match,
                           RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
        }

    }
    private class TestRouteFilter implements RestxRouteFilter, RestxHandler {
        private final String name;

        private TestRouteFilter(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public Optional<RestxHandlerMatch> match(RestxRoute route) {
            return Optional.of(new RestxHandlerMatch(new StdRestxRequestMatch("/*"), this));
        }
        @Override
        public void handle(RestxRequestMatch match,
                           RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
        }

    }

    public static final StdRoute ROUTE = new StdRoute("R1", new StdRestxRequestMatcher("GET", "/test")) {
        @Override
        public void handle(RestxRequestMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
        }

        public String getName() {
            return "ROUTE";
        }
    };
}
