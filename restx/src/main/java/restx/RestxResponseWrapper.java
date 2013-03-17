package restx;

import org.joda.time.Duration;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * User: xavierhanin
 * Date: 3/17/13
 * Time: 3:28 PM
 */
public class RestxResponseWrapper implements RestxResponse {
    private final RestxResponse restxResponse;

    public RestxResponseWrapper(RestxResponse restxResponse) {
        this.restxResponse = restxResponse;
    }

    public void setStatus(int i) {
        restxResponse.setStatus(i);
    }

    public OutputStream getOutputStream() throws IOException {
        return restxResponse.getOutputStream();
    }

    public void setContentType(String s) {
        restxResponse.setContentType(s);
    }

    public void addCookie(String cookie, String value, Duration expires) {
        restxResponse.addCookie(cookie, value, expires);
    }

    public void close() throws Exception {
        restxResponse.close();
    }

    public PrintWriter getWriter() throws IOException {
        return restxResponse.getWriter();
    }

    public void clearCookie(String cookie) {
        restxResponse.clearCookie(cookie);
    }

    public void setHeader(String headerName, String header) {
        restxResponse.setHeader(headerName, header);
    }

    public void addCookie(String cookie, String value) {
        restxResponse.addCookie(cookie, value);
    }
}
