package restx.specs;

import org.assertj.core.groups.Tuple;
import org.joda.time.DateTime;
import org.junit.Test;
import restx.factory.Factory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * User: xavierhanin
 * Date: 5/18/13
 * Time: 5:00 PM
 */
public class RestxSpecLoaderTest {
    @Test
    public void should_load_spec() throws Exception {
        RestxSpec spec = new RestxSpecLoader(Factory.getInstance()).load("cases/test/test.spec.yaml");

        assertThat(spec.getTitle()).isEqualTo("should say hello");
        assertThat(spec.getGiven()).hasSize(2);

        assertThat(spec.getGiven().get(0)).isInstanceOf(GivenTime.class);
        assertThat(((GivenTime) spec.getGiven().get(0)).getTime().getMillis())
                .isEqualTo(DateTime.parse("2013-03-31T14:33:18.272+02:00").getMillis());

        assertThat(spec.getGiven().get(1)).isInstanceOf(GivenUUIDGenerator.class);
        assertThat(((GivenUUIDGenerator) spec.getGiven().get(1)).getPlaybackUUIDs()).containsExactly("123456");

        assertThat(spec.getWhens()).extracting("method", "path").containsExactly(Tuple.tuple("GET", "message/xavier"));
        assertThat(spec.getWhens()).extracting("then").extracting("expectedCode", "expected")
                .containsExactly(Tuple.tuple(200, "{\"message\":\"hello xavier, it's 14:33:18\"}"));
    }
}
