package restx;

import org.joda.time.Duration;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * User: xavierhanin
 * Date: 2/6/13
 * Time: 9:46 PM
 */
public interface RestxResponse extends AutoCloseable {
    void setStatus(HttpStatus i);
    HttpStatus getStatus();

    void setContentType(String s);

    PrintWriter getWriter() throws IOException;

    OutputStream getOutputStream() throws IOException;

    void addCookie(String cookie, String value);
    void addCookie(String cookie, String value, Duration expires);
    void clearCookie(String cookie);

    void setHeader(String headerName, String header);

    /**
     * Sets the log level of this response.
     *
     * @param level the new level
     */
    void setLogLevel(RestxLogLevel level);
    RestxLogLevel getLogLevel();
}
