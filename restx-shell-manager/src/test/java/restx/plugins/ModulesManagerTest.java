package restx.plugins;

import com.google.common.collect.ImmutableList;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * User: xavierhanin
 * Date: 5/4/13
 * Time: 3:06 PM
 */
public class ModulesManagerTest {
    ModulesManager manager = new ModulesManager(ModulesManagerTest.class.getResource("modules.json"));

    @Test
    public void should_find_shell_modules() throws Exception {
        List<ModuleDescriptor> descriptors = manager.searchModules("category=shell");
        assertThat(descriptors).isNotNull().isNotEmpty().hasSize(2);
        assertThat(descriptors.get(0)).isNotNull();
        assertThat(descriptors.get(0).getId()).isEqualTo("io.restx:restx-build-shell:0.2");
    }

    @Test
    public void should_parse_mrid() throws Exception {
        ModuleRevisionId id = manager.toMrid("io.restx:restx-common:0.2");
        assertThat(id).isNotNull();
        assertThat(id.getOrganisation()).isEqualTo("io.restx");
        assertThat(id.getName()).isEqualTo("restx-common");
        assertThat(id.getRevision()).isEqualTo("0.2");
    }

    @Test
    public void should_download_module() throws Exception {
        File toDir = new File("target/tmp");
        delete(toDir);
        manager.download(
                ImmutableList.<ModuleDescriptor>of(new ModuleDescriptor("com.github.kevinsawicki:http-request:0.1", "shell", "")),
                toDir);

        assertThat(toDir.list()).containsExactly("http-request-0.1.jar");
        delete(toDir);
    }

    @Test
    public void should_download_module_and_dependencies() throws Exception {
        File toDir = new File("target/tmp");
        delete(toDir);
        manager.download(
                ImmutableList.<ModuleDescriptor>of(new ModuleDescriptor("commons-httpclient:commons-httpclient:2.0", "shell", "")),
                toDir);

        assertThat(toDir.list()).containsExactly("commons-httpclient-2.0.jar", "commons-lang-1.0.1.jar", "commons-logging-1.0.3.jar");
        delete(toDir);
    }

    void delete(File f) throws IOException {
        if (!f.exists()) {
            return;
        }
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                delete(c);
            }
        }
        if (!f.delete()) {
            throw new FileNotFoundException("Failed to delete file: " + f);
        }
    }
}
