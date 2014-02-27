package restx.security;

import restx.common.Crypto;

/**
 * User: xavierhanin
 * Date: 1/30/13
 * Time: 6:19 PM
 */
public class SignatureKey {
    public static final SignatureKey DEFAULT = new SignatureKey("this is the default signature key".getBytes());
    private final byte[] key;

    public SignatureKey(byte[] key) {
        this.key = key;
    }

    public String sign(String s) {
        return Crypto.sign(s, key);
    }

    public boolean check(String s, String signature) {
        return Crypto.sign(s, key).equals(signature);
    }
}
