package restx;

import com.google.common.base.Optional;

/**
 * User: xavierhanin
 * Date: 1/30/13
 * Time: 6:19 PM
 */
public class SignatureKey {
    private final byte[] key;
    private final Optional<String> appName;

    /**
     * @deprecated Use #SignatureKey(byte[],String) instead to provide
     * a unique appname along your app signature
     */
    @Deprecated
    public SignatureKey(byte[] key) {
        this(key, Optional.<String>absent());
    }

    public SignatureKey(byte[] key, String appName) {
        this(key, Optional.fromNullable(appName));
    }

    SignatureKey(byte[] key, Optional<String> appName) {
        this.key = key;
        this.appName = appName;
    }

    public byte[] getKey() {
        return key;
    }

    public Optional<String> getAppName() {
        return appName;
    }
}
