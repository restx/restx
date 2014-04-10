package restx.common;

import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Date: 1/1/14
 * Time: 22:26
 */
public class MoreObjectsTest {
    @Test
    public void should_to_string_classloader() throws Exception {
        assertThat(MoreObjects.toString(classloader("Test"))).isEqualTo("Test");
        assertThat(MoreObjects.toString(classloader("Test\ntest"))).isEqualTo("Test");
        assertThat(MoreObjects.toString(classloader("Test\r\ntest"))).isEqualTo("Test");
    }

    private ClassLoader classloader(final String s) throws MalformedURLException {
        return new URLClassLoader(new URL[] {new URL("http://localhost/")}) {
            @Override
            public String toString() {
                return s;
            }
        };


    }
}
