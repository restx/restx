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
    String getHttpMethod();
    String getCookieValue(String cookie, String defaultValue);
}
