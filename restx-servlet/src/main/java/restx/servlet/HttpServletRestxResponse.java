package restx.servlet;

import org.joda.time.Duration;
import restx.AbstractResponse;
import restx.RestxResponse;
import restx.http.HttpStatus;

import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * User: xavierhanin
 * Date: 2/6/13
 * Time: 9:40 PM
 */
public class HttpServletRestxResponse extends AbstractResponse<HttpServletResponse> {
    private final HttpServletResponse resp;
    private final HttpServletRequest request;

    public HttpServletRestxResponse(HttpServletResponse resp, HttpServletRequest request) {
        super(HttpServletResponse.class, resp);
        this.resp = resp;
        this.request = request;
    }

    @Override
    protected void doSetStatus(HttpStatus httpStatus) {
        resp.setStatus(httpStatus.getCode());
    }

    @Override
    protected OutputStream doGetOutputStream() throws IOException {
        return resp.getOutputStream();
    }

    @Override
    protected void closeResponse() throws IOException {
    }

    @Override
    public RestxResponse addCookie(String cookie, String value, Duration expiration) {
        Cookie existingCookie = HttpServletRestxRequest.getCookie(request.getCookies(), cookie);
        if (existingCookie != null) {
            if ("/".equals(existingCookie.getPath())) {
                // update existing cookie
                existingCookie.setValue(value);
                existingCookie.setMaxAge(expiration.getStandardSeconds() > 0 ? (int) expiration.getStandardSeconds() : -1);
                resp.addCookie(existingCookie);
            } else {
                // we have an existing cookie on another path: clear it, and add a new cookie on root path
                existingCookie.setValue("");
                existingCookie.setMaxAge(0);
                resp.addCookie(existingCookie);

                Cookie c = new Cookie(cookie, value);
                c.setPath("/");
                c.setMaxAge(expiration.getStandardSeconds() > 0 ? (int) expiration.getStandardSeconds() : -1);
                resp.addCookie(c);
            }
        } else {
            Cookie c = new Cookie(cookie, value);
            c.setPath("/");
            c.setMaxAge(expiration.getStandardSeconds() > 0 ? (int) expiration.getStandardSeconds() : -1);
            resp.addCookie(c);
        }
        return this;
    }

    @Override
    public RestxResponse clearCookie(String cookie) {
        Cookie existingCookie = HttpServletRestxRequest.getCookie(request.getCookies(), cookie);
        if (existingCookie != null) {
            existingCookie.setPath("/");
            existingCookie.setValue("");
            existingCookie.setMaxAge(0);
            resp.addCookie(existingCookie);
        }
        return this;
    }

    @Override
    public RestxResponse setHeader(String headerName, String header) {
        resp.setHeader(headerName, header);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> clazz) {
        if (clazz == HttpServletResponse.class || clazz == ServletResponse.class) {
            return (T) resp;
        }
        throw new IllegalArgumentException("underlying implementation is HttpServletResponse, not " + clazz.getName());
    }
}
