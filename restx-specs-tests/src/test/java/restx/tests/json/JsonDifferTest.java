package restx.tests.json;

import com.google.common.collect.ImmutableMap;
import org.assertj.core.groups.Tuple;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Date: 3/2/14
 * Time: 22:14
 */
public class JsonDifferTest {
    @Test
    public void should_compare_equals() throws Exception {
        JsonDiff compare = new JsonDiffer().compare(new StringJsonSource("", "{}"), new StringJsonSource("", "{}"));

        assertThat(compare).isNotNull();
        assertThat(compare.isSame()).isTrue();
    }

    @Test
    public void should_compare_added_key() throws Exception {
        JsonDiff compare = new JsonDiffer().compare(
                new StringJsonSource("", "{}"),
                new StringJsonSource("", "{\"key1\": \"val1\"}"));

        assertThat(compare).isNotNull();
        assertThat(compare.isSame()).isFalse();
        assertThat(compare.getDifferences()).isNotEmpty()
                .extracting("type", "rightPath", "key", "value", "rightContext.json")
                .containsExactly(Tuple.tuple("ADDED", ".", "key1", "val1", "{\"key1\": \"val1\"}"));
    }

    @Test
    public void should_compare_added_ignored_key() throws Exception {
        JsonDiffer jsonDiffer = new JsonDiffer();
        jsonDiffer.getRightConfig().setIgnoreExtraFields(true);
        JsonDiff compare = jsonDiffer.compare(
                new StringJsonSource("", "{}"),
                new StringJsonSource("", "{\"key1\": \"val1\"}"));

        assertThat(compare).isNotNull();
        assertThat(compare.isSame()).isTrue();
    }

    @Test
    public void should_compare_added_sub_key() throws Exception {
        JsonDiff compare = new JsonDiffer().compare(
                new StringJsonSource("", "{\"key1\": \"val1\", \"key2\": {}}"),
                new StringJsonSource("", "{\"key1\": \"val1\", \"key2\": {\"key3\": \"val3\"}}"));

        assertThat(compare).isNotNull();
        assertThat(compare.isSame()).isFalse();
        assertThat(compare.getDifferences()).isNotEmpty()
                .extracting("type", "leftPath", "key", "value", "rightContext.json")
                .containsExactly(Tuple.tuple("ADDED", "./key2", "key3", "val3", "{\"key3\": \"val3\"}"));
    }

    @Test
    public void should_compare_removed_key() throws Exception {
        JsonDiff compare = new JsonDiffer().compare(
                new StringJsonSource("", "{\"key1\": \"val1\"}"),
                new StringJsonSource("", "{}"));

        assertThat(compare).isNotNull();
        assertThat(compare.isSame()).isFalse();
        assertThat(compare.getDifferences()).isNotEmpty()
                .extracting("type", "leftPath", "key", "value", "leftContext.json")
                .containsExactly(Tuple.tuple("REMOVED", ".", "key1", "val1", "{\"key1\": \"val1\"}"));
    }

    @Test
    public void should_compare_updated_value() throws Exception {
        JsonDiff compare = new JsonDiffer().compare(
                new StringJsonSource("", "{\"key1\": \"val1\"}"),
                new StringJsonSource("", "{\"key1\": \"val2\"}"));

        assertThat(compare).isNotNull();
        assertThat(compare.isSame()).isFalse();
        assertThat(compare.getDifferences()).isNotEmpty()
                .extracting("type", "leftPath", "leftValue", "rightValue",
                        "leftContext.json", "rightContext.json")
                .containsExactly(Tuple.tuple("CHANGED", "./key1", "val1", "val2",
                        "\"val1\"", "\"val2\""));
    }

    @Test
    public void should_compare_nested_updated_value() throws Exception {
        JsonDiff compare = new JsonDiffer().compare(
                new StringJsonSource("", "{\"key1\": \"val1\", \"key2\": {\"key3\": \"val3\"}}"),
                new StringJsonSource("", "{\"key1\": \"val1\", \"key2\": {\"key3\": \"val4\"}}"));

        assertThat(compare).isNotNull();
        assertThat(compare.isSame()).isFalse();
        assertThat(compare.getDifferences()).isNotEmpty()
                .extracting("type", "leftPath", "leftValue", "rightValue",
                        "leftContext.json", "rightContext.json")
                .containsExactly(Tuple.tuple("CHANGED", "./key2/key3", "val3", "val4",
                        "\"val3\"", "\"val4\""));
    }

    @Test
    public void should_compare_added_in_array() throws Exception {
        JsonDiff compare = new JsonDiffer().compare(
                new StringJsonSource("", "{\"key1\": []}"),
                new StringJsonSource("", "{\"key1\": [{\"key2\": \"val2\"}]}"));

        assertThat(compare).isNotNull();
        assertThat(compare.isSame()).isFalse();
        assertThat(compare.getDifferences()).isNotEmpty()
                .extracting(
                        "type", "leftPath",
                        "leftPosition", "values",
                        "leftContext.json", "rightContext.json")
                .containsExactly(Tuple.tuple(
                        "INSERTED", "./key1",
                        0, asList(ImmutableMap.of("key2", "val2")),
                        "[]", "[{\"key2\": \"val2\"}]"));
    }

    @Test
    public void should_compare_deleted_from_array() throws Exception {
        JsonDiff compare = new JsonDiffer().compare(
                new StringJsonSource("", "{\"key1\": [{\"key2\": \"val2\"}]}"),
                new StringJsonSource("", "{\"key1\": []}"));

        assertThat(compare).isNotNull();
        assertThat(compare.isSame()).isFalse();
        assertThat(compare.getDifferences()).isNotEmpty()
                .extracting(
                        "type", "leftPath",
                        "leftPosition", "values",
                        "leftContext.json", "rightContext.json")
                .containsExactly(Tuple.tuple(
                        "DELETED", "./key1",
                        0, asList(ImmutableMap.of("key2", "val2")),
                        "[{\"key2\": \"val2\"}]", "[]"));
    }

    @Test
    public void should_compare_added_key_in_array() throws Exception {
        JsonDiff compare = new JsonDiffer().compare(
                new StringJsonSource("", "{\"key1\": [{}]}"),
                new StringJsonSource("", "{\"key1\": [{\"key2\": \"val2\"}]}"));

        assertThat(compare).isNotNull();
        assertThat(compare.isSame()).isFalse();
        assertThat(compare.getDifferences()).isNotEmpty()
                .extracting("type", "leftPath", "key", "value", "rightContext.json")
                .containsExactly(Tuple.tuple("ADDED", "./key1/[0]", "key2", "val2", "{\"key2\": \"val2\"}"));
    }


    @Test
    public void should_compare_ignore_added_key_in_array() throws Exception {
        JsonDiffer jsonDiffer = new JsonDiffer();
        jsonDiffer.getRightConfig().setIgnoreExtraFields(true);
        JsonDiff compare = jsonDiffer.compare(
                new StringJsonSource("", "{\"key1\": [{}]}"),
                new StringJsonSource("", "{\"key1\": [{\"key2\": \"val2\"}]}"));

        assertThat(compare).isNotNull();
        assertThat(compare.isSame()).isTrue();
    }

    @Test
    public void should_compare_ignore_added_subkey_in_array() throws Exception {
        JsonDiffer jsonDiffer = new JsonDiffer();
        jsonDiffer.getRightConfig().setIgnoreExtraFields(true);
        JsonDiff compare = jsonDiffer.compare(
                new StringJsonSource("", "{\"key1\": [{\"key3\": {}}]}"),
                new StringJsonSource("", "{\"key1\": [{\"key3\": {\"key2\": \"val2\"}}]}"));

        assertThat(compare).isNotNull();
        assertThat(compare.isSame()).isTrue();
    }

    @Test
    public void should_compare_added_key_in_array2() throws Exception {
        JsonDiff compare = new JsonDiffer().compare(
                new StringJsonSource("", "{\"key1\": [{\"key3\": \"val3\"}, {}, {}]}"),
                new StringJsonSource("", "{\"key1\": [{}, {\"key2\": \"val2\"}]}"));

        assertThat(compare).isNotNull();
        assertThat(compare.isSame()).isFalse();
        assertThat(compare.getDifferences()).isNotEmpty();
        assertThat(compare.getDifferences().subList(0, 1))
                .extracting(
                        "type", "leftPath",
                        "leftPosition", "values",
                        "leftContext.json", "rightContext.json")
                .containsExactly(Tuple.tuple(
                        "DELETED", "./key1",
                        0, asList(ImmutableMap.of("key3", "val3")),
                        "[{\"key3\": \"val3\"}, {}, {}]",
                        "[{}, {\"key2\": \"val2\"}]"));

        assertThat(compare.getDifferences().subList(1, 2))
                .extracting("type", "leftPath", "rightPath", "key", "value", "leftContext.json", "rightContext.json")
                .containsExactly(Tuple.tuple("ADDED", "./key1/[2]", "./key1/[1]",
                        "key2", "val2",
                        "{}", "{\"key2\": \"val2\"}"));
    }
}
