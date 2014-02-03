package restx.tests.json;

import com.google.common.base.Optional;
import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Date: 3/2/14
 * Time: 21:38
 */
public class JsonWithLocationsParserTest {
    @Test
    public void should_parse_with_locations_from_string() throws Exception {
        JsonWithLocationsParser.ParsedJsonWithLocations parsed = new JsonWithLocationsParser().parse(
                "{\"test\": { \"k1\": \"val1\" }}", Map.class);

        Object test = ((Map) parsed.getRoot()).get("test");

        Optional<JsonObjectLocation> l = parsed.getLocations().getLocationOf(test);
        assertThat(l.isPresent()).isTrue();
        assertThat(l.get().getFrom().getCharOffset()).isEqualTo(10);
        assertThat(l.get().getTo().getCharOffset()).isEqualTo(25);
        assertThat(l.get().getJson()).isEqualTo("{ \"k1\": \"val1\" }");
    }
}
