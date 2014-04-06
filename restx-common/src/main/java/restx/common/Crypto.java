package restx.common;

import com.google.common.base.Charsets;
import com.google.common.io.BaseEncoding;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Cryptography utils
 */
public class Crypto {

    /**
     * Sign a message with a key
     * @param message The message to sign
     * @param key The key to use
     * @return The signed message (in hexadecimal)
     */
    public static String sign(String message, byte[] key) {
        if (key.length == 0) {
            return message;
        }

        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec signingKey = new SecretKeySpec(key, "HmacSHA1");
            mac.init(signingKey);
            byte[] messageBytes = message.getBytes(Charsets.UTF_8);
            byte[] result = mac.doFinal(messageBytes);
            return BaseEncoding.base64().encode(result);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}