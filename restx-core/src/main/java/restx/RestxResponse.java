package restx;

import org.joda.time.Duration;
import restx.http.HttpStatus;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * User: xavierhanin
 * Date: 2/6/13
 * Time: 9:46 PM
 */
public interface RestxResponse extends AutoCloseable {
    RestxResponse setStatus(HttpStatus i);
    HttpStatus getStatus();

    RestxResponse setContentType(String s);

    PrintWriter getWriter() throws IOException;

    OutputStream getOutputStream() throws IOException;

    RestxResponse addCookie(String cookie, String value);
    RestxResponse addCookie(String cookie, String value, Duration expires);
    RestxResponse clearCookie(String cookie);

    RestxResponse setHeader(String headerName, String header);

    /**
     * Sets the log level of this response.
     *
     * @param level the new level
     */
    RestxResponse setLogLevel(RestxLogLevel level);
    RestxLogLevel getLogLevel();
}
