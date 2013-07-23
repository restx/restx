package restx.classloader;

import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * User: xavierhanin
 * Date: 4/5/13
 * Time: 6:00 PM
 */
public class ApplicationCompilerTest {
    @Test
    public void should_compile_simple_class() throws Exception {
        Class<?> clazz = new ApplicationClassloader(new File("."), "src/test/test-classes-sources")
                .loadClass("restx.classloader.TestSimpleClass");
        assertThat(clazz).isNotNull();

        assertThat(clazz.newInstance().toString()).isEqualTo("it's simple!");
    }
}
