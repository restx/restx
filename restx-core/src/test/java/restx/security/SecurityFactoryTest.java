package restx.security;

import com.google.common.base.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import restx.common.ConfigElement;
import restx.common.StdRestxConfig;

import java.util.ArrayList;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author fcamblor
 */
@RunWith(Parameterized.class)
public class SecurityFactoryTest {

    private static final String COOKIE_PREFIX = "RestxSession";
    private static final String COOKIE_SIGNATURE_PREFIX = "RestxSessionSignature";

    private final String appName;
    private final String expectedCookieName;
    private final String expectedCookieSignatureName;

    @Parameterized.Parameters(name="{0}")
    public static Iterable<Object[]> data(){
        return Arrays.asList(new Object[][]{
            { null, String.format("%s", COOKIE_PREFIX), String.format("%s", COOKIE_SIGNATURE_PREFIX) }, // For keeping backward compat with pre restx-0.2.9 versions
            { "foo", String.format("%s-foo", COOKIE_PREFIX), String.format("%s-foo", COOKIE_SIGNATURE_PREFIX) }, // Most of the cases
            { "foo bar:quix", String.format("%s-foo_bar_quix", COOKIE_PREFIX), String.format("%s-foo_bar_quix", COOKIE_SIGNATURE_PREFIX) } // Some rfc-2616 incompatible cases
        });
    }

    public SecurityFactoryTest(String appName, String expectedCookieName, String expectedCookieSignatureName) {
        this.appName = appName;
        this.expectedCookieName = expectedCookieName;
        this.expectedCookieSignatureName = expectedCookieSignatureName;
    }

    @Test
    public void should_app_named_dependent_cookie_be_correctly_generated(){
        RestxSessionCookieDescriptor cookieDescriptor = new SecurityFactory().restxSessionCookieDescriptor(Optional.fromNullable(this.appName), StdRestxConfig.of(new ArrayList<ConfigElement>()));
        assertThat(cookieDescriptor.getCookieName(), is(equalTo(this.expectedCookieName)));
        assertThat(cookieDescriptor.getCookieSignatureName(), is(equalTo(this.expectedCookieSignatureName)));
    }
}
