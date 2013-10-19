package restx;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.*;


/**
 * User: xavierhanin
 * Date: 1/19/13
 * Time: 9:01 AM
 */
public class StdRouteMatcherTest {
    @Test
    public void should_matcher_with_no_path_param_match_not_match() throws Exception {
        StdRestxRequestMatcher matcher = new StdRestxRequestMatcher("GET", "/user");

        Optional<? extends RestxRequestMatch> match = matcher.match("GET", "/user");
        assertThat(match.isPresent()).isTrue();
        assertThat(match.get().getPathParams()).isEmpty();

        match = matcher.match("POST", "/user");
        assertThat(match.isPresent()).isFalse();

        match = matcher.match("GET", "/user/johndoe");
        assertThat(match.isPresent()).isFalse();

        match = matcher.match("GET", "/use");
        assertThat(match.isPresent()).isFalse();
    }


    @Test
    public void should_matcher_with_one_path_param_match_not_match() throws Exception {
        StdRestxRequestMatcher matcher = new StdRestxRequestMatcher("GET", "/user/{name}");

        Optional<? extends RestxRequestMatch> match = matcher.match("GET", "/user/johndoe");
        assertThat(match.isPresent()).isTrue();
        assertThat(match.get().getPathParams()).isEqualTo(ImmutableMap.of("name", "johndoe"));

        match = matcher.match("POST", "/user/johndoe");
        assertThat(match.isPresent()).isFalse();

        match = matcher.match("GET", "/user/johndoe/details");
        assertThat(match.isPresent()).isFalse();

        match = matcher.match("GET", "/users/johndoe");
        assertThat(match.isPresent()).isFalse();

        match = matcher.match("GET", "/user");
        assertThat(match.isPresent()).isFalse();

        match = matcher.match("GET", "/user/");
        assertThat(match.isPresent()).isFalse();
    }

    @Test
    public void should_matcher_with_several_path_params_match_not_match() throws Exception {
        StdRestxRequestMatcher matcher = new StdRestxRequestMatcher("GET", "/user/{name}/children/{child}");

        Optional<? extends RestxRequestMatch> match = matcher.match("GET", "/user/johndoe/children/bobby");
        assertThat(match.isPresent()).isTrue();
        assertThat(match.get().getPathParams()).isEqualTo(ImmutableMap.of("name", "johndoe", "child", "bobby"));

        match = matcher.match("GET", "/user/johndoe");
        assertThat(match.isPresent()).isFalse();

        match = matcher.match("GET", "/user");
        assertThat(match.isPresent()).isFalse();

        match = matcher.match("GET", "/user/johndoe/children/");
        assertThat(match.isPresent()).isFalse();
    }
}
