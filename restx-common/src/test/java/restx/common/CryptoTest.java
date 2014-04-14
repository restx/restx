package restx.common;

import com.google.common.base.Charsets;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * User: xavierhanin
 * Date: 1/30/13
 * Time: 9:04 AM
 */
public class CryptoTest {
    @Test
    public void testSign() throws Exception {
        String signature = Crypto.sign("My message to sign", "my grain of salt".getBytes(Charsets.UTF_8.name()));

        assertThat(signature)
                .isNotNull()
                .isEqualTo("yIDXrtZ71qCfHnUNvlYSS//0YPE=")
                .isEqualTo(Crypto.sign("My message to sign", "my grain of salt".getBytes(Charsets.UTF_8.name())));
    }
}
