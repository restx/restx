package restx.common;

import com.github.mustachejava.Mustache;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * User: xavierhanin
 * Date: 3/31/13
 * Time: 6:44 PM
 */
public class MustachesTest {
    @Test
    public void should_compile_and_execute() throws Exception {
        Mustache mustache = Mustaches.compile(MustachesTest.class, "MustachesTestFixture1.mustache");
        assertThat(Mustaches.execute(mustache, ImmutableMap.of("name", "world"))).isEqualTo("Hello world");
    }
}
