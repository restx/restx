package restx;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

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
    public String getBaseNetworkPath() {
        return original.getBaseNetworkPath();
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
    public boolean isSecured() {
        return original.isSecured();
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
    public ImmutableMap<String, ImmutableList<String>> getQueryParams() {
        return original.getQueryParams();
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
    public ImmutableMap<String, String> getCookiesMap() {
        return original.getCookiesMap();
    }

    @Override
    public Optional<String> getCookieValue(String cookieName) {
        return original.getCookieValue(cookieName);
    }

    @Override
    public boolean isPersistentCookie(String cookie) {
        return original.isPersistentCookie(cookie);
    }

    @Override
    public String getClientAddress() {
        return original.getClientAddress();
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

    @Override
    public <T> T unwrap(Class<T> clazz) {
        return original.unwrap(clazz);
    }

    @Override
    public Locale getLocale() {
        return original.getLocale();
    }

    @Override
    public ImmutableList<Locale> getLocales() {
        return original.getLocales();
    }

    @Override
    public ImmutableMap<String, String> getHeaders() {
        return original.getHeaders();
    }
}
