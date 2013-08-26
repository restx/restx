package restx.server;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author fcamblor
 */
@RunWith(Parameterized.class)
public class HeaderTokenCompatibleWithRfc2616Test {

    private final String str;
    private final String expectedResult;

    @Parameterized.Parameters(name="{0}")
    public static Iterable<Object[]> data(){
        return Arrays.asList(new Object[][]{
                {"foo", String.format("foo")}, // Most of the cases
                // Cases to apply to rfc-2616 header tokens (p16 on http://www.ietf.org/rfc/rfc2616.txt)
                {"foo bar", "foo_bar"},
                {"foo(bar", "foo_bar"},
                {"foo)bar", "foo_bar"},
                {"foo<bar", "foo_bar"},
                {"foo>bar", "foo_bar"},
                {"foo@bar", "foo_bar"},
                {"foo,bar", "foo_bar"},
                {"foo;bar", "foo_bar"},
                {"foo:bar", "foo_bar"},
                {"foo\\bar", "foo_bar"},
                {"foo\"bar", "foo_bar"},
                {"foo/bar", "foo_bar"},
                {"foo[bar", "foo_bar"},
                {"foo]bar", "foo_bar"},
                {"foo?bar", "foo_bar"},
                {"foo=bar", "foo_bar"},
                {"foo{bar", "foo_bar"},
                {"foo}bar", "foo_bar"}
        });
    }

    public HeaderTokenCompatibleWithRfc2616Test(String str, String expectedResult) {
        this.str = str;
        this.expectedResult = expectedResult;
    }

    @Test
    public void should_given_string_be_correctly_converted_to_rfc2616_compatible_token(){
        assertThat(HTTP.headerTokenCompatible(this.str, "_"), is(equalTo(this.expectedResult)));
    }
}
