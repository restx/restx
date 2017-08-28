package restx.security;

import com.google.common.base.Optional;

import static restx.http.HTTP.headerTokenCompatible;

/**
 * @author fcamblor
 */
public class RestxSessionCookieDescriptor {
    private String cookieName;
    private String cookieSignatureName;
    private String domain;
    private Boolean secure;

    public RestxSessionCookieDescriptor(String cookieName, String cookieSignatureName) {
        this(cookieName, cookieSignatureName, Optional.<String>absent(), Optional.<Boolean>absent());
    }

    public RestxSessionCookieDescriptor(String cookieName, String cookieSignatureName,
                                        Optional<String> domain, Optional<Boolean> secure) {
        this.cookieName = headerTokenCompatible(cookieName, "_");
        this.cookieSignatureName = headerTokenCompatible(cookieSignatureName, "_");
        this.domain = domain.orNull();
        this.secure = secure.orNull();
    }

    public String getCookieName() {
        return cookieName;
    }

    public String getCookieSignatureName() {
        return cookieSignatureName;
    }

    public Optional<String> getDomain() {
        return Optional.fromNullable(domain);
    }

    public Optional<Boolean> getSecure() {
        return Optional.fromNullable(secure);
    }
}
