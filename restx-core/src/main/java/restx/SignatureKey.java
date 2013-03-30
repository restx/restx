package restx;

/**
 * User: xavierhanin
 * Date: 1/30/13
 * Time: 6:19 PM
 */
public class SignatureKey {
    private final byte[] key;

    public SignatureKey(byte[] key) {
        this.key = key;
    }

    public byte[] getKey() {
        return key;
    }
}
