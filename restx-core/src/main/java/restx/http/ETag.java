package restx.http;

import com.google.common.base.Optional;
import restx.RestxRequest;
import restx.RestxResponse;
import restx.WebException;

/**
 * Date: 22/5/14
 * Time: 18:46
 */
public class ETag {
    private final String value;
    private final CacheControl cacheControl;

    public ETag(String value, CacheControl cacheControl) {
        this.value = value;
        this.cacheControl = cacheControl;
    }

    public CacheControl getCacheControl() {
        return cacheControl;
    }

    public String getValue() {
        return value;
    }

    public void handleIn(RestxRequest req, RestxResponse resp) {
        if (req.getHeader("If-None-Match").equals(Optional.of(value))) {
            throw new WebException(HttpStatus.NOT_MODIFIED);
        } else {
            cacheControl.writeTo(resp);
            resp.setHeader("ETag", value);
        }
    }

    @Override
    public String toString() {
        return "ETag{" +
                "value='" + value + '\'' +
                ", cacheControl=" + cacheControl +
                '}';
    }
}
