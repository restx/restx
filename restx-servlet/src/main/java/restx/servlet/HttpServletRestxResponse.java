package restx.servlet;

import com.google.common.base.Optional;
import org.joda.time.Duration;
import restx.http.HttpStatus;
import restx.RestxLogLevel;
import restx.RestxResponse;
import restx.http.HTTP;

import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * User: xavierhanin
 * Date: 2/6/13
 * Time: 9:40 PM
 */
public class HttpServletRestxResponse implements RestxResponse {

    private final HttpServletResponse resp;
    private final HttpServletRequest request;
    private HttpStatus status = HttpStatus.OK;
    private RestxLogLevel logLevel = RestxLogLevel.DEFAULT;

    public HttpServletRestxResponse(HttpServletResponse resp, HttpServletRequest request) {
        this.resp = resp;
        this.request = request;
    }

    @Override
    public HttpStatus getStatus() {
        // HttpServletResponse#getStatus() is available in servlet 3 only.
        //  return resp.getStatus();
        return status;
    }

    @Override
    public RestxResponse setStatus(HttpStatus httpStatus) {
        this.status = httpStatus;
        resp.setStatus(httpStatus.getCode());
        return this;
    }

    @Override
    public RestxResponse setContentType(String s) {
        if (HTTP.isTextContentType(s)) {
            Optional<String> cs = HTTP.charsetFromContentType(s);
            if (!cs.isPresent()) {
                s += "; charset=UTF-8";
            }
        }
        resp.setContentType(s);
        return this;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return resp.getWriter();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return resp.getOutputStream();
    }

    @Override
    public RestxResponse addCookie(String cookie, String value) {
        addCookie(cookie, value, Duration.ZERO);
        return this;
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
    public void close() throws Exception {
    }

    @Override
    public String toString() {
        return "[RESTX RESPONSE] " + status;
    }

    public RestxLogLevel getLogLevel() {
        return logLevel;
    }

    public RestxResponse setLogLevel(RestxLogLevel logLevel) {
        this.logLevel = logLevel;
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
