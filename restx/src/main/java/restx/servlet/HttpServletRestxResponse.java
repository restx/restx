package restx.servlet;

import restx.RestxResponse;

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
    public void setStatus(int i) {
        resp.setStatus(i);
    }

    @Override
    public void setContentType(String s) {
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
        Cookie existingCookie = HttpServletRestxRequest.getCookie(request.getCookies(), cookie);
        if (existingCookie != null) {
            existingCookie.setValue(value);
            resp.addCookie(existingCookie);
        } else {
            resp.addCookie(new Cookie(cookie, value));
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
