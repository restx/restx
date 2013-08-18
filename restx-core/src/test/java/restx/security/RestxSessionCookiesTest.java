package restx.security;

import com.google.common.base.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import restx.SignatureKey;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author fcamblor
 */
@RunWith(Parameterized.class)
public class RestxSessionCookiesTest {

    private static final String COOKIE_PREFIX = "RestxSession";

    private final String appName;
    private final String expectedCookieName;

    @Parameterized.Parameters(name="{0}")
    public static Iterable<Object[]> data(){
        return Arrays.asList(new Object[][]{
            { null, String.format("%s", COOKIE_PREFIX) }, // For keeping backward compat with pre restx-0.2.9 versions
            { "foo", String.format("%s-foo", COOKIE_PREFIX) }, // Most of the cases
            // Cases to apply to rfc-2616 header tokens (p16 on http://www.ietf.org/rfc/rfc2616.txt)
            { "foo bar", String.format("%s-foo_bar", COOKIE_PREFIX) },
            { "foo(bar", String.format("%s-foo_bar", COOKIE_PREFIX) },
            { "foo)bar", String.format("%s-foo_bar", COOKIE_PREFIX) },
            { "foo<bar", String.format("%s-foo_bar", COOKIE_PREFIX) },
            { "foo>bar", String.format("%s-foo_bar", COOKIE_PREFIX) },
            { "foo@bar", String.format("%s-foo_bar", COOKIE_PREFIX) },
            { "foo,bar", String.format("%s-foo_bar", COOKIE_PREFIX) },
            { "foo;bar", String.format("%s-foo_bar", COOKIE_PREFIX) },
            { "foo:bar", String.format("%s-foo_bar", COOKIE_PREFIX) },
            { "foo\\bar", String.format("%s-foo_bar", COOKIE_PREFIX) },
            { "foo\"bar", String.format("%s-foo_bar", COOKIE_PREFIX) },
            { "foo/bar", String.format("%s-foo_bar", COOKIE_PREFIX) },
            { "foo[bar", String.format("%s-foo_bar", COOKIE_PREFIX) },
            { "foo]bar", String.format("%s-foo_bar", COOKIE_PREFIX) },
            { "foo?bar", String.format("%s-foo_bar", COOKIE_PREFIX) },
            { "foo=bar", String.format("%s-foo_bar", COOKIE_PREFIX) },
            { "foo{bar", String.format("%s-foo_bar", COOKIE_PREFIX) },
            { "foo}bar", String.format("%s-foo_bar", COOKIE_PREFIX) }
        });
    }

    public RestxSessionCookiesTest(String appName, String expectedCookieName) {
        this.appName = appName;
        this.expectedCookieName = expectedCookieName;
    }

    @Test
    public void should_app_named_dependent_cookie_be_correctly_generated(){
        assertThat(RestxSessionFilter.appNameDependentCookieKey(COOKIE_PREFIX, new SignatureKey(new byte[0], this.appName)), is(equalTo(this.expectedCookieName)));
    }
}
