package restx.common;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import org.junit.Test;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

/**
 * User: xavierhanin
 * Date: 9/24/13
 * Time: 9:59 PM
 */
public class StdRestxConfigTest {
    @Test
    public void should_access_properties() throws Exception {
        RestxConfig config = StdRestxConfig.of(asList(
                ConfigElement.of("key1", "val1"),
                ConfigElement.of("key2", "2"),
                ConfigElement.of("key3", "true")));

        assertThat(config.getString("key1").isPresent()).isTrue();
        assertThat(config.getString("key1").get()).isEqualTo("val1");
        assertThat(config.getElement("key1").isPresent()).isTrue();
        assertThat(config.getElement("key1").get()).isEqualToComparingFieldByField(ConfigElement.of("key1", "val1"));

        assertThat(config.getString("key2").isPresent()).isTrue();
        assertThat(config.getInt("key2").isPresent()).isTrue();
        assertThat(config.getInt("key2").get()).isEqualTo(2);

        assertThat(config.getString("key3").isPresent()).isTrue();
        assertThat(config.getBoolean("key3").isPresent()).isTrue();
        assertThat(config.getBoolean("key3").get()).isTrue();

        assertThat(config.getString("key4").isPresent()).isFalse();

        assertThat(config.elements()).extracting("key", "value").containsExactly(
                tuple("key1", "val1"), tuple("key2", "2"), tuple("key3", "true"));
    }

    @Test
    public void should_empty_be_absent() throws Exception {
        RestxConfig config = StdRestxConfig.of(asList(
                ConfigElement.of("key1", "")));

        assertThat(config.getString("key1").isPresent()).isFalse();
        assertThat(config.getInt("key1").isPresent()).isFalse();
        assertThat(config.getBoolean("key1").isPresent()).isFalse();
        assertThat(config.getElement("key1").isPresent()).isTrue();
        assertThat(config.getElement("key1").get()).isEqualToComparingFieldByField(ConfigElement.of("key1", ""));

        assertThat(config.elements()).extracting("key", "value").containsExactly(tuple("key1", ""));
    }

    @Test
    public void should_first_defined_override() throws Exception {
        RestxConfig config = StdRestxConfig.of(asList(
                ConfigElement.of("key1", "val1"),
                ConfigElement.of("key1", "val2")));

        assertThat(config.getString("key1").isPresent()).isTrue();
        assertThat(config.getString("key1").get()).isEqualTo("val1");
        assertThat(config.getElement("key1").isPresent()).isTrue();
        assertThat(config.getElement("key1").get()).isEqualToComparingFieldByField(ConfigElement.of("key1", "val1"));

        assertThat(config.elements()).extracting("key", "value").containsExactly(tuple("key1", "val1"));
    }

    @Test
    public void should_merge_doc() throws Exception {
        RestxConfig config = StdRestxConfig.of(asList(
                ConfigElement.of("source1", "", "key1", "val1"),
                ConfigElement.of("source2", "doc2", "key1", "val2")));

        assertThat(config.getString("key1").isPresent()).isTrue();
        assertThat(config.getString("key1").get()).isEqualTo("val1");
        assertThat(config.getElement("key1").isPresent()).isTrue();
        assertThat(config.getElement("key1").get()).isEqualToComparingFieldByField(
                ConfigElement.of("source1", "doc2", "key1", "val1"));
    }

    @Test
    public void should_parse_properties() throws Exception {
        RestxConfig config = StdRestxConfig.parse(
                "restx/common/config.properties", Resources.asCharSource(
                Resources.getResource(
                    "restx/common/config.properties"), Charsets.UTF_8));

        assertThat(config.getString("key1").isPresent()).isTrue();
        assertThat(config.getString("key1").get()).isEqualTo("val1");
        assertThat(config.getElement("key1").isPresent()).isTrue();
        assertThat(config.getElement("key1").get()).isEqualToComparingFieldByField(
                ConfigElement.of("restx/common/config.properties", "Doc 1", "key1", "val1"));

        assertThat(config.getString("key2").isPresent()).isTrue();
        assertThat(config.getInt("key2").isPresent()).isTrue();
        assertThat(config.getInt("key2").get()).isEqualTo(2);
        assertThat(config.getElement("key2").get()).isEqualToComparingFieldByField(
                ConfigElement.of("restx/common/config.properties", "Doc 2\non 2 lines", "key2", "2"));
        
        assertThat(config.getString("key3").isPresent()).isTrue();
        assertThat(config.getString("key3").get()).isEqualTo("val1+2=3\\o/ \nthis was a smiley!");
        assertThat(config.getElement("key3").isPresent()).isTrue();
        assertThat(config.getElement("key3").get()).isEqualToComparingFieldByField(
                ConfigElement.of("restx/common/config.properties", "Doc 3\nLogical line", "key3", "val1+2=3\\o/ \nthis was a smiley!"));
    }
}
