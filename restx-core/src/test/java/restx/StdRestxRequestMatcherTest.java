package restx;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;


/**
 * User: xavierhanin
 * Date: 1/19/13
 * Time: 9:01 AM
 */
public class StdRestxRequestMatcherTest {
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
        assertThat(match.get().getPathParam("name")).isEqualTo("johndoe");

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

    @Test
    public void should_matcher_with_several_path_params_and_custom_regex_match_not_match() throws Exception {
        StdRestxRequestMatcher matcher = new StdRestxRequestMatcher("GET", "/user/{name:[A-Z]{1,3}\\d}/children/{child:.+}");

        Optional<? extends RestxRequestMatch> match;

        match = matcher.match("GET", "/user/johndoe/children/bobby");
        assertThat(match.isPresent()).isFalse();

        match = matcher.match("GET", "/user/JOHNDOE/children/bobby");
        assertThat(match.isPresent()).isFalse();

        match = matcher.match("GET", "/user/JOH1/children/bobby");
        assertThat(match.isPresent()).isTrue();
        assertThat(match.get().getPathParams()).isEqualTo(ImmutableMap.of("name", "JOH1", "child", "bobby"));

        match = matcher.match("GET", "/user/JOH2/children/bobby/and/the/rest");
        assertThat(match.isPresent()).isTrue();
        assertThat(match.get().getPathParams()).isEqualTo(ImmutableMap.of("name", "JOH2", "child", "bobby/and/the/rest"));

        match = matcher.match("GET", "/user/johndoe");
        assertThat(match.isPresent()).isFalse();

        match = matcher.match("GET", "/user");
        assertThat(match.isPresent()).isFalse();

        match = matcher.match("GET", "/user/johndoe/children/");
        assertThat(match.isPresent()).isFalse();
    }

    @Test
    public void should_empty_custom_regex_fail() throws Exception {
        try {
            new StdRestxRequestMatcher("GET", "/user/{name:}");
            fail("empty custom regex should raise an exception");
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessageContaining("/user/{name:}");
        }
    }

    @Test
    public void should_non_letter_in_group_name_fail() throws Exception {
        try {
            new StdRestxRequestMatcher("GET", "/user/{name+}");
            fail("non letter in custom regex should raise an exception");
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessageContaining("/user/{name+}");
        }
    }

    @Test
    public void should_matcher_with_several_path_params_with_column_notation_match_not_match() throws Exception {
        StdRestxRequestMatcher matcher;
        Optional<? extends RestxRequestMatch> match;

        matcher = new StdRestxRequestMatcher("GET", "/user/:name/children/:child");
        match = matcher.match("GET", "/user/johndoe/children/bobby");
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
