package restx.security;

import com.chrylis.codec.base58.Base58Codec;
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
    private Boolean encode;

    public RestxSessionCookieDescriptor(String cookieName, String cookieSignatureName, Boolean encode) {
        this(cookieName, cookieSignatureName, Optional.<String>absent(), Optional.<Boolean>absent(), encode);
    }

    public RestxSessionCookieDescriptor(String cookieName, String cookieSignatureName,
                                        Optional<String> domain, Optional<Boolean> secure, Boolean encode) {
        this.cookieName = headerTokenCompatible(cookieName, "_");
        this.cookieSignatureName = headerTokenCompatible(cookieSignatureName, "_");
        this.domain = domain.orNull();
        this.secure = secure.orNull();
        this.encode = encode;
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

    public Boolean getEncode() {
        return encode;
    }

    public String encodeValueIfNeeded(String value) {
        return encode && value != null ? Base58Codec.doEncode(value.getBytes()) : value;
    }

    public String decodeValueIfNeeded(String value) {
        return encode && value != null ? new String(Base58Codec.doDecode(value)) : value;
    }
}
