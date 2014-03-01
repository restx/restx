package restx;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.http.HTTP;
import restx.http.HttpStatus;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Date: 1/3/14
 * Time: 20:46
 */
public abstract class AbstractResponse<R> implements RestxResponse {
    private static final Logger logger = LoggerFactory.getLogger(AbstractResponse.class);

    private final Class<R> responseClass;
    private final R response;

    private HttpStatus status = HttpStatus.OK;
    private String charset;
    private PrintWriter writer;
    private OutputStream outputStream;
    private RestxLogLevel logLevel = RestxLogLevel.DEFAULT;

    protected AbstractResponse(Class<R> responseClass, R response) {
        this.responseClass = responseClass;
        this.response = response;
    }

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public RestxResponse setStatus(HttpStatus httpStatus) {
        this.status = httpStatus;
        doSetStatus(httpStatus);
        return this;
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
        setHeader("Content-Type", s);
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
                new OutputStreamWriter(doGetOutputStream(), charset), true);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (outputStream != null) {
            return outputStream;
        }
        return outputStream = doGetOutputStream();
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
        closeResponse();
    }

    public RestxLogLevel getLogLevel() {
        return logLevel;
    }

    public RestxResponse setLogLevel(RestxLogLevel logLevel) {
        this.logLevel = logLevel;
        return this;
    }

    @Override
    public RestxResponse addCookie(String cookie, String value) {
        addCookie(cookie, value, Duration.ZERO);
        return this;
    }

    @Override
    public String toString() {
        return "[RESTX RESPONSE] " + status;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> clazz) {
        if (clazz == this.responseClass) {
            return (T) response;
        }
        throw new IllegalArgumentException("underlying implementation is " + this.responseClass.getName()
                + ", not " + clazz.getName());
    }

    protected abstract void closeResponse() throws IOException;

    protected abstract OutputStream doGetOutputStream() throws IOException;

    protected abstract void doSetStatus(HttpStatus httpStatus);
}
