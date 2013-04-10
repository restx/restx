package restx;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * User: xavierhanin
 * Date: 1/22/13
 * Time: 2:49 PM
 */
public interface RestxRequest {
    /**
     * Returns the base URI of this request.
     * Eg http://mydomain.com/api
     *
     * @return the base URI of this request.
     */
    String getBaseUri();
    /**
     * Returns the restx portion of the request path.
     * <p>
     * If incoming request is http://mydomain.com/api/myresource/test?q=test and baseUri is http://mydomain.com/api,
     * then restx path will be /myresource/test
     * </p>
     * @return the restx portion of the request path.
     */
    String getRestxPath();
    /**
     * Returns the restx portion of the full request uri.
     * <p>
     * If incoming request is http://mydomain.com/api/myresource/test?q=test and baseUri is http://mydomain.com/api,
     * then restx uri will be /myresource/test?q=test
     * </p>
     * @return the restx portion of the full request uri.
     */
    String getRestxUri();

    /**
     * HTTP METHOD, eg GET, POST, ...
     * @return the request HTTP method
     */
    String getHttpMethod();

    Optional<String> getQueryParam(String param);
    List<String> getQueryParams(String param);
    ImmutableMap<String, ImmutableList<String>> getQueryParams();

    Optional<String> getHeader(String headerName);
    String getContentType();

    String getCookieValue(String cookie, String defaultValue);
    boolean isPersistentCookie(String cookie);
    Map<String,String> getCookiesMap();

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

}
