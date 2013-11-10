package restx.security;

import static restx.http.HTTP.headerTokenCompatible;

/**
 * @author fcamblor
 */
public class RestxSessionCookieDescriptor {
    private String cookieName;
    private String cookieSignatureName;

    public RestxSessionCookieDescriptor(String cookieName, String cookieSignatureName) {
        this.cookieName = headerTokenCompatible(cookieName, "_");
        this.cookieSignatureName = headerTokenCompatible(cookieSignatureName, "_");
    }

    public String getCookieName() {
        return cookieName;
    }

    public String getCookieSignatureName() {
        return cookieSignatureName;
    }
}
