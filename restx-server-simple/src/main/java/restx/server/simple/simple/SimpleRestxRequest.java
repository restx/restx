package restx.server.simple.simple;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.simpleframework.http.Cookie;
import org.simpleframework.http.Query;
import org.simpleframework.http.Request;
import restx.AbstractRequest;
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
public class SimpleRestxRequest  extends AbstractRequest {
    private final String restxPath;
    private final String apiPath;
    private final Request request;
    private BufferedInputStream bufferedInputStream;
    private ImmutableMap<String, ImmutableList<String>> queryParams;

    public SimpleRestxRequest(String apiPath, Request request) {
        this.apiPath = apiPath;
        this.request = request;
        String path = request.getTarget().substring(apiPath.length());
        if (path.indexOf("?") != -1) {
            path = path.substring(0, path.indexOf("?"));
        }
        this.restxPath = path;
    }

    @Override
    public int getPort() {
        String host = request.getValue("Host");
        if (host.indexOf(":") != -1) {
            return Integer.parseInt(host.substring(host.indexOf(":") + 1));
        } else {
            return 80;
        }
    }

    @Override
    public String getBaseUri() {
        return "http://" + request.getValue("Host") + apiPath;
    }

    @Override
    public String getBaseNetworkPath() {
        return request.getValue("Host") + apiPath;
    }

    @Override
    public String getRestxPath() {
        return restxPath;
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
        return Lists.newArrayList(request.getQuery().getAll(param));
    }

    @Override
    public ImmutableMap<String, ImmutableList<String>> getQueryParams() {
        if (queryParams == null) {
            ImmutableMap.Builder<String, ImmutableList<String>> paramsBuilder = ImmutableMap.builder();
            Query query = request.getQuery();
            for (String param : query.keySet()) {
                paramsBuilder.put(param, ImmutableList.copyOf(query.getAll(param)));
            }
            queryParams = paramsBuilder.build();
        }
        return queryParams;
    }

    @Override
    public Optional<String> getHeader(String headerName) {
        return Optional.fromNullable(request.getValue(headerName));
    }

    @Override
    public String getContentType() {
        return request.getContentType().getType();
    }

    @Override
    public ImmutableMap<String, String> getCookiesMap() {
        Map<String, String> cookies = Maps.newLinkedHashMap();
        for (Cookie cookie : request.getCookies()) {
            cookies.put(cookie.getName(), cookie.getValue().replace("\\\"", "\""));
        }
        return ImmutableMap.copyOf(cookies);
    }

    @Override
    public Optional<String> getCookieValue(String cookieName) {
        return Optional.fromNullable(getCookiesMap().get(cookieName));
    }

    @Override
    public boolean isPersistentCookie(String cookie) {
        Map<String, String> cookiesMap = getCookiesMap();
        return cookiesMap.containsKey(cookie) ? request.getCookie(cookie).getExpiry() > 0 : false;
    }

    @Override
    public String getClientAddress() {
        return request.getClientAddress().toString();
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
}
