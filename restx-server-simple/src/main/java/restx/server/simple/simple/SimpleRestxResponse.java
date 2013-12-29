package restx.server.simple.simple;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import org.joda.time.Duration;
import org.simpleframework.http.Cookie;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.http.HttpStatus;
import restx.RestxLogLevel;
import restx.RestxResponse;
import restx.http.HTTP;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * User: xavierhanin
 * Date: 2/16/13
 * Time: 1:57 PM
 */
public class SimpleRestxResponse implements RestxResponse {
    private static final Logger logger = LoggerFactory.getLogger(SimpleRestxResponse.class);
    private final Response response;
    private String charset;
    private PrintWriter writer;
    private OutputStream outputStream;
    private RestxLogLevel logLevel = RestxLogLevel.DEFAULT;

    public SimpleRestxResponse(Response response) {
        this.response = response;
    }

    @Override
    public RestxResponse setStatus(HttpStatus status) {
        response.setCode(status.getCode());
        return this;
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.havingCode(response.getStatus().code);
    }

    @Override
    public RestxResponse setContentType(String s) {
        if (HTTP.isTextContentType(s)) {
            Optional<String> cs = HTTP.charsetFromContentType(s);
            if (!cs.isPresent()) {
                s += "; charset=UTF-8";
                charset = Charsets.UTF_8.name();
            } else {
                charset = cs.get();
            }
        }
        response.setValue("Content-Type", s);
        return this;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer != null) {
            return writer;
        }

        if (charset == null) {
            logger.warn("no charset defined while getting writer to write http response." +
                    " Make sure you call setContentType() before calling getWriter(). Using UTF-8 charset.");
            charset = Charsets.UTF_8.name();
        }
        return writer = new PrintWriter(
                new OutputStreamWriter(response.getPrintStream(), charset), true);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (outputStream != null) {
            return outputStream;
        }
        return outputStream = response.getOutputStream();
    }

    @Override
    public RestxResponse addCookie(String cookie, String value) {
        addCookie(cookie, value, Duration.ZERO);
        return this;
    }

    @Override
    public RestxResponse addCookie(String cookie, String value, Duration expiration) {
        Cookie c = new Cookie(cookie, value, "/");
        c.setExpiry(expiration.getStandardSeconds() > 0 ? (int) expiration.getStandardSeconds() : -1);
        response.setCookie(c);
        return this;
    }

    @Override
    public RestxResponse clearCookie(String cookie) {
        Cookie c = new Cookie(cookie, "");
        c.setPath("/");
        c.setExpiry(0);
        response.setCookie(c);
        return this;
    }

    @Override
    public RestxResponse setHeader(String headerName, String header) {
        response.setValue(headerName, header);
        return this;
    }

    @Override
    public void close() throws Exception {
        if (writer != null) {
            writer.println();
            writer.close();
        }
        if (outputStream != null) {
            outputStream.close();
        }
        response.close();
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
        if (clazz == Response.class) {
            return (T) response;
        }
        throw new IllegalArgumentException("underlying implementation is " + Response.class.getName()
                + ", not " + clazz.getName());
    }

}
