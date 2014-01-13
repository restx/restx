package restx;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

/**
 * User: xavierhanin
 * Date: 1/22/13
 * Time: 2:49 PM
 */
public interface RestxRequest {
    /**
     * Returns the base URI of this request.
     * Eg http://mydomain.com/api or http://mydomain.com:8080
     *
     * When used behind a proxy, this will try to return the client facing URI, by using:
     * - X-Forwarded-Host for the host
     * - X-Forwarded-Proto for the scheme
     * - checking first Via to know if request was made in HTTPS
     *
     * see http://en.wikipedia.org/wiki/X-Forwarded-For
     * see http://en.wikipedia.org/wiki/List_of_HTTP_header_fields
     * see http://httpd.apache.org/docs/current/mod/mod_proxy.html#proxyvia
     *
     * @return the base URI of this request.
     */
    String getBaseUri();

    /**
     * Returns the base network path of this request (ie baseUri without the protocol).
     * Eg //mydomain.com/api or //mydomain.com:8080
     *
     * This is useful to create paths using the same protocol as the one seen by the client, as opposed
     * to the protocol seen by the server (server can see http if you have a front http server like Apache
     * doing https and reverse proxy).
     *
     * See also this discussion:
     * http://stackoverflow.com/questions/5799577/does-using-www-example-com-in-javascript-chose-http-https-protocol-automatical
     *
     * Note that if Via headers are set getBaseUri should be fine too.
     *
     * @return the base network path of this request.
     */
    String getBaseNetworkPath();

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
     * Is this request performed through a secured connection or not.
     *
     * This will return true if:
     * - the HttpSettings proto() is set to 'https'
     * - the request has a 'X-Forwarded-Proto' header with value 'https', and comes from an authorized proxy
     *   as defined by HttpSettings.forwardedSupport()
     * - the request was performed in HTTPS on this server
     *
     * @return true if this request is performed through a secured (HTTPS) connection.
     */
    boolean isSecured();

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

    Optional<String> getCookieValue(String cookieName);
    boolean isPersistentCookie(String cookie);
    ImmutableMap<String,String> getCookiesMap();

    /**
     * The address (IP) of the client.
     *
     * If X-Forwarded-For header is present, it will return its value, otherwise it returns
     * the remote client address.
     *
     * see http://httpd.apache.org/docs/current/mod/mod_proxy.html#x-headers for details on this header.
     *
     * @return
     */
    String getClientAddress();

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

    /**
     * Unwraps the underlying native implementation of given class.
     *
     * Examnple: This is a HttpServletRequest in a servlet container.
     *
     * @param clazz the class of the underlying implementation
     * @param <T>
     * @return the unwrapped implementation.
     * @throws java.lang.IllegalArgumentException if the underlying implementation is not of given type.
     */
    <T> T unwrap(Class<T> clazz);

    Locale getLocale();
    List<Locale> getLocales();
}
