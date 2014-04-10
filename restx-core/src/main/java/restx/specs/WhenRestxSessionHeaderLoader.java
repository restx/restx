package restx.specs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.factory.Component;
import restx.security.RestxSessionCookieDescriptor;
import restx.security.Signer;

/**
 * @author fcamblor
 */
@Component
public class WhenRestxSessionHeaderLoader implements RestxSpecLoader.WhenHeaderLoader {

    private static final Logger logger = LoggerFactory.getLogger(WhenRestxSessionHeaderLoader.class);

    private final RestxSessionCookieDescriptor restxSessionCookieDescriptor;
    private final Signer signer;

    public WhenRestxSessionHeaderLoader(RestxSessionCookieDescriptor restxSessionCookieDescriptor, Signer signer) {
        this.restxSessionCookieDescriptor = restxSessionCookieDescriptor;
        this.signer = signer;
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
        whenHttpRequestBuilder.addCookie(restxSessionCookieDescriptor.getCookieSignatureName(), signer.sign(sessionContent));
    }
}
