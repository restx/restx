package restx;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import org.joda.time.Duration;
import restx.http.HttpStatus;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;

/**
 * A wrapper delegating all calls to an underlying response.
 *
 * Useful for implementing response wrapper by extending this class.
 *
 * Note that all but one method are delegated to underlying response.
 * The exception is the getWriter() method, which builds a writer using
 * getOutputStream() and underlying getCharset(). This is the standard way
 * to implement getWriter(), and it makes it much easier to wrap the response
 * outputstream.
 */
public class RestxResponseWrapper implements RestxResponse {
    private final RestxResponse restxResponse;
    private PrintWriter writer;

    public RestxResponseWrapper(RestxResponse restxResponse) {
        this.restxResponse = restxResponse;
    }

    public PrintWriter getWriter() throws IOException {
        return writer = new PrintWriter(
                new OutputStreamWriter(getOutputStream(), getCharset().or(Charsets.UTF_8)), true);
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
        if (writer != null) {
            try {
                writer.close();
            } finally {
                writer = null;
            }
        }
        restxResponse.close();
    }

    @Override
    public boolean isClosed() {
        return restxResponse.isClosed();
    }

    public RestxResponse clearCookie(String cookie) {
        restxResponse.clearCookie(cookie);
        return this;
    }

    public RestxResponse setHeader(String headerName, String header) {
        restxResponse.setHeader(headerName, header);
        return this;
    }

    @Override
    public Optional<String> getHeader(String headerName) {
        return restxResponse.getHeader(headerName);
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
