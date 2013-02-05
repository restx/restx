package restx;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * User: xavierhanin
 * Date: 1/19/13
 * Time: 9:01 AM
 */
public class StdRouteMatcherTest {
    @Test
    public void should_matcher_with_no_path_param_match_not_match() throws Exception {
        StdRouteMatcher matcher = new StdRouteMatcher("GET", "/user");

        Optional<RestxRouteMatch> match = matcher.match("GET", "/user");
        assertThat(match.isPresent(), is(true));
        assertThat(match.get().getPathParams().size(), is(0));

        match = matcher.match("POST", "/user");
        assertThat(match.isPresent(), is(false));

        match = matcher.match("GET", "/user/johndoe");
        assertThat(match.isPresent(), is(false));

        match = matcher.match("GET", "/use");
        assertThat(match.isPresent(), is(false));
    }


    @Test
    public void should_matcher_with_one_path_param_match_not_match() throws Exception {
        StdRouteMatcher matcher = new StdRouteMatcher("GET", "/user/{name}");

        Optional<RestxRouteMatch> match = matcher.match("GET", "/user/johndoe");
        assertThat(match.isPresent(), is(true));
        assertThat(match.get().getPathParams(), is(equalTo(ImmutableMap.of("name", "johndoe"))));

        match = matcher.match("POST", "/user/johndoe");
        assertThat(match.isPresent(), is(false));

        match = matcher.match("GET", "/user/johndoe/details");
        assertThat(match.isPresent(), is(false));

        match = matcher.match("GET", "/users/johndoe");
        assertThat(match.isPresent(), is(false));

        match = matcher.match("GET", "/user");
        assertThat(match.isPresent(), is(false));

        match = matcher.match("GET", "/user/");
        assertThat(match.isPresent(), is(false));
    }

    @Test
    public void should_matcher_with_several_path_params_match_not_match() throws Exception {
        StdRouteMatcher matcher = new StdRouteMatcher("GET", "/user/{name}/children/{child}");

        Optional<RestxRouteMatch> match = matcher.match("GET", "/user/johndoe/children/bobby");
        assertThat(match.isPresent(), is(true));
        assertThat(match.get().getPathParams(), is(equalTo(ImmutableMap.of("name", "johndoe", "child", "bobby"))));

        match = matcher.match("GET", "/user/johndoe");
        assertThat(match.isPresent(), is(false));

        match = matcher.match("GET", "/user");
        assertThat(match.isPresent(), is(false));

        match = matcher.match("GET", "/user/johndoe/children/");
        assertThat(match.isPresent(), is(false));
    }
}
