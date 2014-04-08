package restx.i18n;

import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Date: 25/1/14
 * Time: 14:58
 */
public class DefaultMessagesTest {

    @Before
    public void setUp() throws Exception {
        Locale.setDefault(Locale.US);
    }

    @Test
    public void should_translate_simple() throws Exception {
        DefaultMessages m = new DefaultMessages("restx.i18n.test");

        assertThat(m.getMessage("key1", Locale.ENGLISH)).isEqualTo("Hello!");
        assertThat(m.getMessage("key1", Locale.FRENCH)).isEqualTo("Bonjour !");
    }

    @Test
    public void should_translate_with_params() throws Exception {
        DefaultMessages m = new DefaultMessages("restx.i18n.test");

        assertThat(m.getMessage("key2", MessageParams.of("who", "World"), Locale.ENGLISH)).isEqualTo("Hello World!");
        assertThat(m.getMessage("key2", MessageParams.of("who", "World"), Locale.FRENCH)).isEqualTo("Bonjour World !");
    }

    @Test
    public void should_list_keys() throws Exception {
        DefaultMessages m = new DefaultMessages("restx.i18n.test");

        assertThat(m.keys()).containsOnly("key1", "key2");
    }

    @Test
    public void should_access_values() throws Exception {
        DefaultMessages m = new DefaultMessages("restx.i18n.test");

        assertThat(m.getMessageTemplate("key2", Locale.ENGLISH)).isEqualTo("Hello {{who}}!");
        assertThat(m.getMessageTemplate("key2", Locale.FRENCH)).isEqualTo("Bonjour {{who}} !");
    }
}
