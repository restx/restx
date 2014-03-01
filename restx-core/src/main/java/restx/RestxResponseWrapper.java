package restx;

import com.google.common.base.Optional;
import org.joda.time.Duration;
import restx.http.HttpStatus;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;

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

    public HttpStatus getStatus() {
        return restxResponse.getStatus();
    }

    public RestxResponse setStatus(HttpStatus i) {
        restxResponse.setStatus(i);
        return this;
    }

    @Override
    public Optional<Charset> getCharset() {
        return restxResponse.getCharset();
    }

    public OutputStream getOutputStream() throws IOException {
        return restxResponse.getOutputStream();
    }

    public RestxResponse setContentType(String s) {
        restxResponse.setContentType(s);
        return this;
    }

    public RestxResponse addCookie(String cookie, String value, Duration expires) {
        restxResponse.addCookie(cookie, value, expires);
        return this;
    }

    public void close() throws Exception {
        restxResponse.close();
    }

    public PrintWriter getWriter() throws IOException {
        return restxResponse.getWriter();
    }

    public RestxResponse clearCookie(String cookie) {
        restxResponse.clearCookie(cookie);
        return this;
    }

    public RestxResponse setHeader(String headerName, String header) {
        restxResponse.setHeader(headerName, header);
        return this;
    }

    public RestxResponse addCookie(String cookie, String value) {
        restxResponse.addCookie(cookie, value);
        return this;
    }

    @Override
    public RestxResponse setLogLevel(RestxLogLevel level) {
        restxResponse.setLogLevel(level);
        return this;
    }

    @Override
    public RestxLogLevel getLogLevel() {
        return restxResponse.getLogLevel();
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        return restxResponse.unwrap(clazz);
    }
}
