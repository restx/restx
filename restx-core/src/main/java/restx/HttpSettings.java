package restx;

import com.google.common.base.Optional;
import restx.config.SettingsKey;

import java.util.Collection;

/**
 * Date: 2/1/14
 * Time: 13:48
 */
public interface HttpSettings {
    @SettingsKey(key = "restx.http.XForwardedSupport", defaultValue = "127.0.0.1")
    Collection<String> forwardedSupport();

    @SettingsKey(key = "restx.http.host")
    Optional<String> host();

    @SettingsKey(key = "restx.http.scheme")
    Optional<String> scheme();

    @SettingsKey(key = "restx.http.gzip.paths", defaultValue = "/{s:.+}")
    Collection<String> gzipPaths();

    @SettingsKey(key = "restx.http.decode.url.path.params", defaultValue = "true",
            doc="Will issue a URLDecoder.decode() on every PATH parameters if true")
    boolean decodeURLPathParams();
}
