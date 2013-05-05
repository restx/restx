package restx.exceptions;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.Test;
import restx.common.UUIDGenerator;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;

/**
 * User: xavierhanin
 * Date: 3/19/13
 * Time: 3:04 PM
 */
public class RestxErrorTest {
    @ErrorCode(code = "999", description = "My error occurs only during tests")
    private static enum MyError {
        @ErrorField("my field") MY_FIELD;
    }

    @Test
    public void should_provide_error_message() throws Exception {
        UUIDGenerator.playback(Arrays.asList("123456"));
        DateTimeUtils.setCurrentMillisFixed(DateTime.parse("2013-03-19T12:00:00Z").getMillis());
        try {
            RestxError.RestxException restxException = RestxError.on(MyError.class).set(MyError.MY_FIELD, "my value").raise();
            assertThat(restxException).isNotNull();
            assertThat(restxException.getMessage()).isNotNull().isEqualTo(
                    "[2013-03-19T12:00:00.000Z] [123456] [400~999] My error occurs only during tests - {MY_FIELD=my value}");
        } finally {
            DateTimeUtils.setCurrentMillisSystem();
            UUIDGenerator.useDefault();
        }
    }
}
