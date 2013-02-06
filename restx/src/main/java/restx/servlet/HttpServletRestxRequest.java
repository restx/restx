package restx.servlet;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import restx.RestxRequest;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * User: xavierhanin
 * Date: 1/22/13
 * Time: 2:52 PM
 */
public class HttpServletRestxRequest implements RestxRequest {
    private final HttpServletRequest request;

    public HttpServletRestxRequest(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public String getRestxPath() {
        return request.getRequestURI().substring(
                (request.getContextPath() + request.getServletPath()).length());
    }

    @Override
    public Optional<String> getQueryParam(String param) {
        return Optional.fromNullable(request.getParameter(param));
    }

    @Override
    public List<String> getQueryParams(String param) {
        return Lists.newArrayList(request.getParameterValues(param));
    }

    @Override
    public InputStream getContentStream() throws IOException {
        return request.getInputStream();
    }

    @Override
    public String getHttpMethod() {
        return request.getMethod();
    }

    @Override
    public String getCookieValue(String cookieName, String defaultValue) {
        return getCookieValue(request.getCookies(), cookieName, defaultValue);
    }

    private static String getCookieValue(Cookie[] cookies,
                                          String cookieName,
                                          String defaultValue) {
        if (cookies == null) {
            return defaultValue;
        }
        for (int i = 0; i < cookies.length; i++) {
            Cookie cookie = cookies[i];
            if (cookieName.equals(cookie.getName()))
                return cookie.getValue();
        }
        return defaultValue;
    }

}
