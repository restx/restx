package restx.specs;

import com.google.common.base.Splitter;
import restx.factory.Component;

/**
 * @author fcamblor
 */
@Component
public class WhenCookieHeaderLoader implements RestxSpecLoader.WhenHeaderLoader {
    @Override
    public String detectionPattern() {
        return "Cookie:";
    }

    @Override
    public void loadHeader(String headerValue, WhenHttpRequest.Builder whenHttpRequestBuilder) {
        for (String s : Splitter.on(";").trimResults().split(headerValue)) {
            int i = s.indexOf('=');

            String name = s.substring(0, i);
            String value = s.substring(i + 1);
            whenHttpRequestBuilder.addCookie(name, value);
        }
    }
}
