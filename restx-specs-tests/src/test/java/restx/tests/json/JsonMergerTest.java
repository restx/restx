package restx.tests.json;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 * Date: 3/2/14
 * Time: 22:14
 */
public class JsonMergerTest {
    JsonMerger merger = new JsonMerger();

    @Test
    public void should_merge_added_key_to_right() throws Exception {
        JsonDiff compare = new JsonDiffer().compare(
                new StringJsonSource("", "{}"),
                new StringJsonSource("", "{\"key1\": \"val1\"}"));

        assertThat(merger.mergeToRight(compare)).isEqualTo("{ }");
    }

    @Test
    public void should_merge_added_key_to_left() throws Exception {
        JsonDiff compare = new JsonDiffer().compare(
                new StringJsonSource("", "{}"),
                new StringJsonSource("", "{\"key1\": \"val1\"}"));

        assertThat(merger.mergeToLeft(compare)).isEqualTo(format("{%n  \"key1\" : \"val1\"%n}"));
    }

    @Test
    public void should_merge_with_ignored_keys() throws Exception {
        JsonDiffer jsonDiffer = new JsonDiffer();
        jsonDiffer.getLeftConfig().setIgnoreExtraFields(true);
        JsonDiff compare = jsonDiffer.compare(
                new StringJsonSource("", "{\"key1\": \"val1\", \"key2\": \"val2\"}"),
                new StringJsonSource("", "{\"key1\": \"otherval\"}"));

        assertThat(merger.mergeToRight(compare)).isEqualTo(format("{%n  \"key1\" : \"val1\"%n}"));
    }

    @Test
    public void should_merge_added_sub_key_to_right() throws Exception {
        JsonDiff compare = new JsonDiffer().compare(
                new StringJsonSource("", "{\"key1\": \"val1\", \"key2\": {}}"),
                new StringJsonSource("", "{\"key1\": \"val1\", \"key2\": {\"key3\": \"val3\"}}"));

        assertThat(merger.mergeToRight(compare)).isEqualTo(format("{%n  \"key1\" : \"val1\",%n  \"key2\" : { }%n}"));
    }

    @Test
    public void should_merge_added_sub_key_to_left() throws Exception {
        JsonDiff compare = new JsonDiffer().compare(
                new StringJsonSource("", "{\"key1\": \"val1\", \"key2\": {}}"),
                new StringJsonSource("", "{\"key1\": \"val1\", \"key2\": {\"key3\": \"val3\"}}"));

        assertThat(merger.mergeToLeft(compare)).isEqualTo(format(
                "{%n  \"key1\" : \"val1\",%n  \"key2\" : {%n    \"key3\" : \"val3\"%n  }%n}"));
    }
    
    @Test
    public void should_merge_added_in_array() throws Exception {
        JsonDiff compare = new JsonDiffer().compare(
                new StringJsonSource("", "{\"key1\": []}"),
                new StringJsonSource("", "{\"key1\": [{\"key2\": \"val2\"}]}"));

        String mergeToRight = merger.mergeToRight(compare);
		assertThat(mergeToRight).isEqualTo(format("{%n  \"key1\" : [ ]%n}"));
    }
    

    @Test
    public void should_merge_deleted_from_array_with_ignored_keys() throws Exception {
        JsonDiffer jsonDiffer = new JsonDiffer();
        jsonDiffer.getLeftConfig().setIgnoreExtraFields(true);
        JsonDiff compare = jsonDiffer.compare(
                new StringJsonSource("", "{\"key1\": [{\"key2\": \"val21\", \"key3\": \"val31\"}, {\"key2\": \"val22\", \"key3\": \"val32\"}]}"),
                new StringJsonSource("", "{\"key1\": [{\"key2\": \"val22\"}]}"));

        String mergeToRight = merger.mergeToRight(compare);
		assertThat(mergeToRight).isEqualTo(format("{%n" +
                "  \"key1\" : [ {%n" +
                "    \"key2\" : \"val21\",%n" +
                "    \"key3\" : \"val31\"%n" + // deducing that key3 is an ignored key from the other val is too error prone, we don't handle that
                "  }, {%n" +
                "    \"key2\" : \"val22\"%n" +
                "  } ]%n" +
                "}"));
    }

    @Test
    public void should_merge_deleted_from_array() throws Exception {
        JsonDiff compare = new JsonDiffer().compare(
                new StringJsonSource("", "{\"key1\": [{\"key2\": \"val2\"}]}"),
                new StringJsonSource("", "{\"key1\": []}"));

        assertThat(merger.mergeToRight(compare)).isEqualTo(format("{%n" +
                "  \"key1\" : [ {%n" +
                "    \"key2\" : \"val2\"%n" +
                "  } ]%n" +
                "}"));
    }

}
