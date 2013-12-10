package restx.exceptions;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.Test;
import restx.CoreModule;
import restx.common.UUIDGenerator;
import restx.factory.Factory;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;
import static restx.common.UUIDGenerator.PlaybackUUIDGenerator.playbackUUIDs;

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
        DateTimeUtils.setCurrentMillisFixed(DateTime.parse("2013-03-19T12:00:00Z").getMillis());
        try {
            RestxErrors errors = new RestxErrors(playbackUUIDs("123456"));
            RestxError.RestxException restxException = errors.on(MyError.class).set(MyError.MY_FIELD, "my value").raise();
            assertThat(restxException).isNotNull();
            assertThat(restxException.getMessage()).isNotNull().isEqualTo(
                    "[2013-03-19T12:00:00.000Z] [123456] [400~999] My error occurs only during tests - {MY_FIELD=my value}");
        } finally {
            DateTimeUtils.setCurrentMillisSystem();
        }
    }
}
