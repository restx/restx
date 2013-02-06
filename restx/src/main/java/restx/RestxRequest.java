package restx;

import com.google.common.base.Optional;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * User: xavierhanin
 * Date: 1/22/13
 * Time: 2:49 PM
 */
public interface RestxRequest {
    String getRestxPath();
    Optional<String> getQueryParam(String param);
    List<String> getQueryParams(String param);
    InputStream getContentStream() throws IOException;

    /**
     * Closes the request content input stream.
     * <p>
     * Closing the content stream using the close method may not definitely close it, in case a buffered input stream
     * has been used to provide access to request content for logs and error processing.
     * </p>
     * <p>
     * Restx framework will always call this method at the end of request processing.
     * </p>
     *
     * @throws IOException
     */
    void closeContentStream() throws IOException;
    String getHttpMethod();
    String getCookieValue(String cookie, String defaultValue);
}
