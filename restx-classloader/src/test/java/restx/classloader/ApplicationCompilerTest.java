package restx.classloader;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * User: xavierhanin
 * Date: 4/5/13
 * Time: 6:00 PM
 */
public class ApplicationCompilerTest {
    @Test
    public void should_compile_simple_class() throws Exception {
        Play.init("src/test/test-classes-sources");
        Class<?> clazz = Play.classloader.loadClass("restx.classloader.TestSimpleClass");
        assertThat(clazz).isNotNull();

        assertThat(clazz.newInstance().toString()).isEqualTo("it's simple!");
    }
}
