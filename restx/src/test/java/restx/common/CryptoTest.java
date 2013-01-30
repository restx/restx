package restx.common;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * User: xavierhanin
 * Date: 1/30/13
 * Time: 9:04 AM
 */
public class CryptoTest {
    @Test
    public void testSign() throws Exception {
        String signature = Crypto.sign("My message to sign", "my grain of salt".getBytes("UTF-8"));

        assertThat(signature, notNullValue());
        assertThat(signature, equalTo("yIDXrtZ71qCfHnUNvlYSS//0YPE="));
        assertThat(signature, equalTo(Crypto.sign("My message to sign", "my grain of salt".getBytes("UTF-8"))));
    }
}
