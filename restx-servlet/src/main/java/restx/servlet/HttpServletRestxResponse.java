package restx.servlet;

import org.joda.time.Duration;
import restx.AbstractResponse;
import restx.RestxResponse;
import restx.http.HttpStatus;
import restx.security.RestxSessionCookieDescriptor;

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
    public RestxResponse addCookie(String cookie, String value, RestxSessionCookieDescriptor cookieDescriptor, Duration expiration) {
        Cookie existingCookie = HttpServletRestxRequest.getCookie(request.getCookies(), cookie);
        String encodeValue = cookieDescriptor.encodeValueIfNeeded(value);

        if (existingCookie != null) {
            if(cookieDescriptor.getDomain().isPresent()) {
                existingCookie.setDomain(cookieDescriptor.getDomain().get());
            }
            if(cookieDescriptor.getSecure().isPresent()) {
                existingCookie.setSecure(cookieDescriptor.getSecure().get().booleanValue());
            }
            if ("/".equals(existingCookie.getPath())
                    || existingCookie.getPath() == null // in some cases cookies set on path '/' are returned with a null path
                    ) {
                // update existing cookie
                existingCookie.setPath("/");
                existingCookie.setValue(encodeValue);
                existingCookie.setMaxAge(expiration.getStandardSeconds() > 0 ? (int) expiration.getStandardSeconds() : -1);
                resp.addCookie(existingCookie);
            } else {
                // we have an existing cookie on another path: clear it, and add a new cookie on root path
                existingCookie.setValue("");
                existingCookie.setMaxAge(0);
                resp.addCookie(existingCookie);

                Cookie c = new Cookie(cookie, encodeValue);
                c.setPath("/");
                c.setMaxAge(expiration.getStandardSeconds() > 0 ? (int) expiration.getStandardSeconds() : -1);
                resp.addCookie(c);
            }
        } else {
            Cookie c = new Cookie(cookie, encodeValue);
            c.setPath("/");
            c.setMaxAge(expiration.getStandardSeconds() > 0 ? (int) expiration.getStandardSeconds() : -1);
            if(cookieDescriptor.getDomain().isPresent()) {
                c.setDomain(cookieDescriptor.getDomain().get());
            }
            if(cookieDescriptor.getSecure().isPresent()) {
                c.setSecure(cookieDescriptor.getSecure().get().booleanValue());
            }
            resp.addCookie(c);
        }
        return this;
    }

    @Override
    public RestxResponse clearCookie(String cookie, RestxSessionCookieDescriptor cookieDescriptor) {
        Cookie existingCookie = HttpServletRestxRequest.getCookie(request.getCookies(), cookie);
        if (existingCookie != null) {
            existingCookie.setPath("/");
            existingCookie.setValue("");
            existingCookie.setMaxAge(0);
            if(cookieDescriptor.getDomain().isPresent()) {
                existingCookie.setDomain(cookieDescriptor.getDomain().get());
            }
            if(cookieDescriptor.getSecure().isPresent()) {
                existingCookie.setSecure(cookieDescriptor.getSecure().get().booleanValue());
            }
            resp.addCookie(existingCookie);
        }
        return this;
    }

    @Override
    public void doSetHeader(String headerName, String header) {
        resp.setHeader(headerName, header);
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
