package restx.i18n;

import com.google.common.io.Files;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Date: 25/1/14
 * Time: 15:55
 */
public class DefaultMutableMessagesTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void should_update_bundle() throws Exception {
        final File file = folder.newFile();
        Files.copy(new File("src/test/resources/restx/i18n/test.properties"), file);

        DefaultMutableMessages m = new DefaultMutableMessages("restx.i18n.test") {
            @Override
            protected URL getResource(String resourceName) {
                try {
                    return file.toURI().toURL();
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        assertThat(m.getMessage("key1", Locale.ENGLISH)).isEqualTo("Hello!");
        m.setMessageTemplate("key1", "Greetings!", Locale.ENGLISH);
        assertThat(m.getMessage("key1", Locale.ENGLISH)).isEqualTo("Greetings!");

        assertThat(file).hasContent("key1=Greetings!\nkey2=Hello {{who}}!");
    }
}
