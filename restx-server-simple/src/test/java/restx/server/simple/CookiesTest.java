package restx.server.simple;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.simpleframework.http.*;
import restx.server.simple.simple.SimpleRestxRequest;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author fcamblor
 */
public class CookiesTest {
    @Test
    public void shouldCookiesFetchedCorrectly() throws InterruptedException {
        SimpleRestxRequest restxRequest = createRestxRequest(new Cookie("foo", "valueWhenFound"));
        assertThat(restxRequest.getCookieValue("foo").or("valueWhenNotFound"), is(equalTo("valueWhenFound")));
        assertThat(restxRequest.getCookieValue("unexistingCookie").or("valueWhenNotFound"), is(equalTo("valueWhenNotFound")));
    }

    @Test
    public void shouldCookieExpirationBeCorrectlyCalculated() throws InterruptedException {
        SimpleRestxRequest restxRequest = createRestxRequest(
                persistentCookie("fastlyExpiredCookie", "fastlyExpiredCookieValue", 1),
                new Cookie("foo", "fooVal")
        );
        assertThat(restxRequest.isPersistentCookie("foo"), is(false));
        assertThat(restxRequest.isPersistentCookie("fastlyExpiredCookie"), is(true));
    }

    private static SimpleRestxRequest createRestxRequest(Cookie... cookies) {
        Request simpleRequest = createSimpleRequestMock(cookies);
        return new SimpleRestxRequest("/foo", simpleRequest);
    }

    private static Request createSimpleRequestMock(Cookie... cookies) {
        Request simpleRequest = mock(Request.class);
        when(simpleRequest.getTarget()).thenReturn("/foo");
        when(simpleRequest.getCookies()).thenReturn(Lists.newArrayList(cookies));
        for(Cookie cookie : cookies){
            when(simpleRequest.getCookie(cookie.getName())).thenReturn(cookie);
        }
        return simpleRequest;
    }

    private static Cookie persistentCookie(String name, String value, int expiry) {
        Cookie persistentCookie = new Cookie(name, value);
        persistentCookie.setExpiry(expiry);
        return persistentCookie;
    }
}
