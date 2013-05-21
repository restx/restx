package restx.servlet;

import com.google.common.base.Optional;
import org.joda.time.Duration;
import restx.RestxResponse;
import restx.server.HTTP;

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

    public HttpServletRestxResponse(HttpServletResponse resp, HttpServletRequest request) {
        this.resp = resp;
        this.request = request;
    }

    @Override
    public int getStatus() {
        return resp.getStatus();
    }

    @Override
    public void setStatus(int i) {
        resp.setStatus(i);
    }

    @Override
    public void setContentType(String s) {
        if (HTTP.isTextContentType(s)) {
            Optional<String> cs = HTTP.charsetFromContentType(s);
            if (!cs.isPresent()) {
                s += "; charset=UTF-8";
            }
        }
        resp.setContentType(s);
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
    public void addCookie(String cookie, String value) {
        addCookie(cookie, value, Duration.ZERO);
    }

    @Override
    public void addCookie(String cookie, String value, Duration expiration) {
        Cookie existingCookie = HttpServletRestxRequest.getCookie(request.getCookies(), cookie);
        if (existingCookie != null) {
            existingCookie.setValue(value);
            existingCookie.setMaxAge(expiration.getStandardSeconds() > 0 ? (int) expiration.getStandardSeconds() : -1);
            resp.addCookie(existingCookie);
        } else {
            Cookie c = new Cookie(cookie, value);
            c.setPath("/");
            c.setMaxAge(expiration.getStandardSeconds() > 0 ? (int) expiration.getStandardSeconds() : -1);
            resp.addCookie(c);
        }
    }

    @Override
    public void clearCookie(String cookie) {
        Cookie existingCookie = HttpServletRestxRequest.getCookie(request.getCookies(), cookie);
        if (existingCookie != null) {
            existingCookie.setPath("/");
            existingCookie.setValue("");
            existingCookie.setMaxAge(0);
            resp.addCookie(existingCookie);
        }
    }

    @Override
    public void setHeader(String headerName, String header) {
        resp.setHeader(headerName, header);
    }

    @Override
    public void close() throws Exception {
    }

    @Override
    public String toString() {
        return "[RESTX RESPONSE] " + resp.getStatus();
    }
}
