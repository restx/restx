package restx.server.simple;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.simpleframework.http.Cookie;
import org.simpleframework.http.Request;
import restx.RestxRequest;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * User: xavierhanin
 * Date: 2/16/13
 * Time: 1:57 PM
 */
public class SimpleRestxRequest implements RestxRequest {
    private String apiPath;
    private Request request;
    private BufferedInputStream bufferedInputStream;

    public SimpleRestxRequest(String apiPath, Request request) {
        this.apiPath = apiPath;
        this.request = request;
    }

    @Override
    public String getBaseUri() {
        return "http://" + request.getValue("Host") + apiPath;
    }

    @Override
    public String getRestxPath() {
        return request.getPath().getRelative(apiPath + "/");
    }

    @Override
    public String getRestxUri() {
        return getRestxPath() + (request.getQuery().isEmpty() ? "" : "?" + request.getQuery().toString());
    }

    @Override
    public String getHttpMethod() {
        return request.getMethod();
    }

    @Override
    public Optional<String> getQueryParam(String param) {
        return Optional.fromNullable(request.getQuery().get(param));
    }

    @Override
    public List<String> getQueryParams(String param) {
        return Lists.newArrayList(request.getQuery().keySet());
    }

    @Override
    public Optional<String> getHeader(String headerName) {
        return Optional.fromNullable(request.getValue(headerName));
    }

    @Override
    public Map<String, String> getCookiesMap() {
        Map<String, String> cookies = Maps.newLinkedHashMap();
        for (Cookie cookie : request.getCookies()) {
            cookies.put(cookie.getName(), cookie.getValue());
        }
        return cookies;
    }

    @Override
    public String getCookieValue(String cookie, String defaultValue) {
        return request.getCookies().contains(cookie) ? request.getCookie(cookie).getValue() : defaultValue;
    }

    @Override
    public boolean isPersistentCookie(String cookie) {
        return request.getCookies().contains(cookie) ? request.getCookie(cookie).getExpiry() > 0 : false;
    }

    @Override
    public InputStream getContentStream() throws IOException {
        /*
           maybe we could do this buffering only in dev mode?
           It is used to be able to read data again in case of json processing error.
         */
        if (bufferedInputStream == null) {
            bufferedInputStream = new BufferedInputStream(request.getInputStream()) {
                @Override
                public void close() throws IOException {
                    // NO OP, see #closeContentStream
                }
            };
            bufferedInputStream.mark(10 * 1024);
        }
        return bufferedInputStream;
    }

    @Override
    public void closeContentStream() throws IOException {
        bufferedInputStream.close();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[RESTX REQUEST] ");
        sb.append(getHttpMethod()).append(" ").append(getRestxPath());
        dumpParameters(sb);
        return sb.toString();
    }

    private void dumpParameters(StringBuilder sb) {
        if (request.getQuery().isEmpty()) {
            return;
        }
        sb.append(" ? ");
        for (String key : request.getQuery().keySet()) {
            List<String> values = request.getQuery().getAll(key);
            sb.append(key).append("=").append(
                    values.size() == 1
                            ? values.get(0)
                            : Joiner.on("&" + key + "=").join(values));
            sb.append("&");
        }
        sb.setLength(sb.length() - 1);
    }

}
