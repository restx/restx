package restx;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.io.IOException;

import static org.fest.assertions.api.Assertions.assertThat;


/**
 * User: xavierhanin
 * Date: 1/19/13
 * Time: 9:01 AM
 */
public class StdRouteMatcherTest {
    private RestxHandler handler = new RestxHandler() {
        @Override
        public Optional<RestxRouteMatch> match(RestxRequest req) {
            return Optional.absent();
        }

        @Override
        public void handle(RestxRouteMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
        }
    };

    @Test
    public void should_matcher_with_no_path_param_match_not_match() throws Exception {
        StdRouteMatcher matcher = new StdRouteMatcher("GET", "/user");

        Optional<RestxRouteMatch> match = matcher.match(handler, "GET", "/user");
        assertThat(match.isPresent()).isTrue();
        assertThat(match.get().getPathParams()).isEmpty();

        match = matcher.match(handler, "POST", "/user");
        assertThat(match.isPresent()).isFalse();

        match = matcher.match(handler, "GET", "/user/johndoe");
        assertThat(match.isPresent()).isFalse();

        match = matcher.match(handler, "GET", "/use");
        assertThat(match.isPresent()).isFalse();
    }


    @Test
    public void should_matcher_with_one_path_param_match_not_match() throws Exception {
        StdRouteMatcher matcher = new StdRouteMatcher("GET", "/user/{name}");

        Optional<RestxRouteMatch> match = matcher.match(handler, "GET", "/user/johndoe");
        assertThat(match.isPresent()).isTrue();
        assertThat(match.get().getPathParams()).isEqualTo(ImmutableMap.of("name", "johndoe"));

        match = matcher.match(handler, "POST", "/user/johndoe");
        assertThat(match.isPresent()).isFalse();

        match = matcher.match(handler, "GET", "/user/johndoe/details");
        assertThat(match.isPresent()).isFalse();

        match = matcher.match(handler, "GET", "/users/johndoe");
        assertThat(match.isPresent()).isFalse();

        match = matcher.match(handler, "GET", "/user");
        assertThat(match.isPresent()).isFalse();

        match = matcher.match(handler, "GET", "/user/");
        assertThat(match.isPresent()).isFalse();
    }

    @Test
    public void should_matcher_with_several_path_params_match_not_match() throws Exception {
        StdRouteMatcher matcher = new StdRouteMatcher("GET", "/user/{name}/children/{child}");

        Optional<RestxRouteMatch> match = matcher.match(handler, "GET", "/user/johndoe/children/bobby");
        assertThat(match.isPresent()).isTrue();
        assertThat(match.get().getPathParams()).isEqualTo(ImmutableMap.of("name", "johndoe", "child", "bobby"));

        match = matcher.match(handler, "GET", "/user/johndoe");
        assertThat(match.isPresent()).isFalse();

        match = matcher.match(handler, "GET", "/user");
        assertThat(match.isPresent()).isFalse();

        match = matcher.match(handler, "GET", "/user/johndoe/children/");
        assertThat(match.isPresent()).isFalse();
    }
}
