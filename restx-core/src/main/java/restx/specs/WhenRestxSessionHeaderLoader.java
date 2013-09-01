package restx.specs;

import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.SignatureKey;
import restx.common.Crypto;
import restx.factory.Component;
import restx.security.RestxSessionCookieDescriptor;

/**
 * @author fcamblor
 */
@Component
public class WhenRestxSessionHeaderLoader implements RestxSpecLoader.WhenHeaderLoader {

    private static final Logger logger = LoggerFactory.getLogger(WhenRestxSessionHeaderLoader.class);

    private final RestxSessionCookieDescriptor restxSessionCookieDescriptor;
    private final SignatureKey signature;

    public WhenRestxSessionHeaderLoader(RestxSessionCookieDescriptor restxSessionCookieDescriptor, Optional<SignatureKey> signature) {
        this.restxSessionCookieDescriptor = restxSessionCookieDescriptor;
        this.signature = signature.or(SignatureKey.DEFAULT);
    }

    @Override
    public String detectionPattern() {
        return "$RestxSession:";
    }

    @Override
    public void loadHeader(String headerValue, WhenHttpRequest.Builder whenHttpRequestBuilder) {
        String sessionContent = headerValue.trim();

        if(whenHttpRequestBuilder.containsCookie(restxSessionCookieDescriptor.getCookieName())
                || whenHttpRequestBuilder.containsCookie(restxSessionCookieDescriptor.getCookieSignatureName())){
            logger.warn("Restx session cookie will be overwritten by {} special header !", detectionPattern());
        }

        whenHttpRequestBuilder.addCookie(restxSessionCookieDescriptor.getCookieName(), sessionContent);
        whenHttpRequestBuilder.addCookie(restxSessionCookieDescriptor.getCookieSignatureName(), Crypto.sign(sessionContent, signature.getKey()));
    }
}
