package restx.specs;

import org.assertj.core.groups.Tuple;
import org.joda.time.DateTime;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * User: xavierhanin
 * Date: 5/18/13
 * Time: 5:00 PM
 */
public class RestxSpecLoaderTest {
    @Test
    public void should_load_spec() throws Exception {
        RestxSpec spec = new RestxSpecLoader().load("cases/test/test.spec.yaml");

        assertThat(spec.getTitle()).isEqualTo("should say hello");
        assertThat(spec.getGiven()).extracting("time").extracting("millis")
                .containsExactly(DateTime.parse("2013-03-31T14:33:18.272+02:00").getMillis());

        assertThat(spec.getWhens()).extracting("method", "path").containsExactly(Tuple.tuple("GET", "message/xavier"));
        assertThat(spec.getWhens()).extracting("then").extracting("expectedCode", "expected")
                .containsExactly(Tuple.tuple(200, "{\"message\":\"hello xavier, it's 14:33:18\"}"));
    }
}
