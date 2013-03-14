package restx.server.simple;

import com.google.common.base.Optional;
import org.joda.time.Duration;
import org.simpleframework.http.Cookie;
import org.simpleframework.http.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.RestxResponse;
import restx.server.HTTP;

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
    private final Logger logger = LoggerFactory.getLogger(SimpleRestxResponse.class);
    private final Response response;
    private String charset;
    private PrintWriter writer;
    private OutputStream outputStream;

    public SimpleRestxResponse(Response response) {
        this.response = response;
    }

    @Override
    public void setStatus(int status) {
        response.setCode(status);
    }

    @Override
    public void setContentType(String s) {
        if (HTTP.isTextContentType(s)) {
            Optional<String> cs = HTTP.charsetFromContentType(s);
            if (!cs.isPresent()) {
                s += "; charset=UTF-8";
                charset = "UTF-8";
            } else {
                charset = cs.get();
            }
        }
        response.setValue("Content-Type", s);
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (charset == null) {
            logger.warn("no charset defined while getting writer to write http response." +
                    " Make sure you call setContentType() before calling getWriter(). Using UTF-8 charset.");
            charset = "UTF-8";
        }
        return writer = new PrintWriter(
                new OutputStreamWriter(response.getPrintStream(), charset), true);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return outputStream = response.getOutputStream();
    }

    @Override
    public void addCookie(String cookie, String value) {
        addCookie(cookie, value, Duration.ZERO);
    }

    @Override
    public void addCookie(String cookie, String value, Duration expiration) {
        Cookie c = new Cookie(cookie, value);
        c.setExpiry(expiration.getStandardSeconds() > 0 ? (int) expiration.getStandardSeconds() : -1);
        response.setCookie(c);
    }

    @Override
    public void clearCookie(String cookie) {
        Cookie c = new Cookie(cookie, "");
        c.setExpiry(0);
        response.setCookie(c);
    }

    @Override
    public void setHeader(String headerName, String header) {
        response.setValue(headerName, header);
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
}
