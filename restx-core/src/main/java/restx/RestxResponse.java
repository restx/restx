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
 * Date: 2/6/13
 * Time: 9:46 PM
 */
public interface RestxResponse extends AutoCloseable {
    RestxResponse setStatus(HttpStatus i);
    HttpStatus getStatus();

    /**
     * Sets the content type of this response.
     *
     * It is highly recommended to call this before writing the response content, especially if you want to use getWriter().
     *
     * The response charset may be set when calling this, either to the provided charset in the content type,
     * or by default to UTF-8 if it is a 'text' content type.
     *
     * @param s the content type
     * @return the current response
     */
    RestxResponse setContentType(String s);

    /**
     * Returns the charset set on this response if any.
     *
     * The charset is set when calling setContentType, either to the provided charset in the content type,
     * or by default to UTF-8 if it is a 'text' content type.
     *
     * @return the optional charset
     */
    Optional<Charset> getCharset();

    /**
     * A writer you can write to to send response as text.
     *
     * The charset used is the one returned by getCharset(), or UTF-8 if not set.
     *
     * It is strongly recommended to call setContentType to set the charset before calling this method.
     *
     * @return a PrintWriter which can be used to write the response.
     *
     * @throws IOException
     */
    PrintWriter getWriter() throws IOException;

    OutputStream getOutputStream() throws IOException;

    RestxResponse addCookie(String cookie, String value);
    RestxResponse addCookie(String cookie, String value, Duration expires);
    RestxResponse clearCookie(String cookie);

    RestxResponse setHeader(String headerName, String header);

    /**
     * Returns the value of a header previously set with setHeader().
     *
     * @param headerName the name of the header to get.
     * @return the header value.
     */
    Optional<String> getHeader(String headerName);

    /**
     * Sets the log level of this response.
     *
     * @param level the new level
     * @return self
     */
    RestxResponse setLogLevel(RestxLogLevel level);
    RestxLogLevel getLogLevel();

    /**
     * Unwraps the underlying native implementation of given class.
     *
     * Examnple: This is a HttpServletRequest in a servlet container.
     *
     * @param clazz the class of the underlying implementation
     * @param <T> unwrapped class
     * @return the unwrapped implementation.
     * @throws java.lang.IllegalArgumentException if the underlying implementation is not of given type.
     */
    <T> T unwrap(Class<T> clazz);

    boolean isClosed();

}
