package restx.common;

import com.google.common.collect.ImmutableMap;
import com.samskivert.mustache.Template;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * User: xavierhanin
 * Date: 3/31/13
 * Time: 6:44 PM
 */
public class MustachesTest {
    @Test
    public void should_compile_and_execute() throws Exception {
        Template mustache = Mustaches.compile(MustachesTest.class, "MustachesTestFixture1.mustache");
        assertThat(Mustaches.execute(mustache, ImmutableMap.of("name", "world"))).isEqualTo("Hello world");
    }
}
