package restx.http;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import restx.RestxResponse;

/**
 * Date: 22/5/14
 * Time: 18:47
 */
public class CacheControl {
    public static final CacheControl MUST_REVALIDATE = new CacheControl(-1, ImmutableList.of("must-revalidate"));

    private final int expires;
    private final ImmutableList<String> cacheControl;

    public CacheControl(int expires, ImmutableList<String> cacheControl) {
        this.expires = expires;
        this.cacheControl = cacheControl;
    }

    public int getExpires() {
        return expires;
    }

    public ImmutableList<String> getCacheControl() {
        return cacheControl;
    }

    @Override
    public String toString() {
        return "CacheControl{" +
                "expires=" + expires +
                ", cacheControl=" + cacheControl +
                '}';
    }

    public void writeTo(RestxResponse resp) {
        resp.setHeader("Expires", String.valueOf(expires));
        resp.setHeader("Cache-Control", Joiner.on(", ").join(cacheControl));
    }
}
