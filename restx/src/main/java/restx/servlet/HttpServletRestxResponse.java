package restx.servlet;

import restx.RestxResponse;

import javax.servlet.http.Cookie;
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

    public HttpServletRestxResponse(HttpServletResponse resp) {
        this.resp = resp;
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
    public void addCookie(Cookie cookie) {
        resp.addCookie(cookie);
    }

    @Override
    public String toString() {
        return "[RESTX RESPONSE] " + resp.getStatus();
    }
}
