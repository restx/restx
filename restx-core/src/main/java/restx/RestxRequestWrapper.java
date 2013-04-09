package restx;

import com.google.common.base.Optional;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * User: xavierhanin
 * Date: 3/17/13
 * Time: 3:26 PM
 */
public class RestxRequestWrapper implements RestxRequest {
    private final RestxRequest original;

    public RestxRequestWrapper(RestxRequest original) {
        this.original = original;
    }

    @Override
    public String getBaseUri() {
        return original.getBaseUri();
    }

    @Override
    public String getRestxPath() {
        return original.getRestxPath();
    }

    @Override
    public String getRestxUri() {
        return original.getRestxUri();
    }

    @Override
    public String getHttpMethod() {
        return original.getHttpMethod();
    }

    @Override
    public Optional<String> getQueryParam(String param) {
        return original.getQueryParam(param);
    }

    @Override
    public List<String> getQueryParams(String param) {
        return original.getQueryParams(param);
    }

    @Override
    public Optional<String> getHeader(String headerName) {
        return original.getHeader(headerName);
    }

    @Override
    public String getContentType() {
        return original.getContentType();
    }

    @Override
    public Map<String, String> getCookiesMap() {
        return original.getCookiesMap();
    }

    @Override
    public String getCookieValue(String cookie, String defaultValue) {
        return original.getCookieValue(cookie, defaultValue);
    }

    @Override
    public boolean isPersistentCookie(String cookie) {
        return original.isPersistentCookie(cookie);
    }

    @Override
    public InputStream getContentStream() throws IOException {
        return original.getContentStream();
    }

    @Override
    public void closeContentStream() throws IOException {
        original.closeContentStream();
    }

    @Override
    public String toString() {
        return original.toString();
    }
}
