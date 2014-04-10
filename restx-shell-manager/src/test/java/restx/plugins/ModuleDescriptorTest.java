package restx.plugins;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * User: xavierhanin
 * Date: 5/4/13
 * Time: 5:43 PM
 */
public class ModuleDescriptorTest {
    @Test
    public void should_return_module_id() throws Exception {
        assertThat(new ModuleDescriptor("io.restx:restx-core:0.2", "description", "shell").getModuleId())
                .isEqualTo("io.restx:restx-core");
    }
}
