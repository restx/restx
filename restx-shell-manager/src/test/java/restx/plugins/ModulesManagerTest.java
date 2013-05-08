package restx.plugins;

import com.google.common.collect.ImmutableList;
import org.apache.ivy.core.module.id.ModuleId;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
    public void should_parse_mid() throws Exception {
        ModuleId id = manager.toModuleId("io.restx:restx-common");
        assertThat(id).isNotNull();
        assertThat(id.getOrganisation()).isEqualTo("io.restx");
        assertThat(id.getName()).isEqualTo("restx-common");
    }

    @Test
    public void should_download_module() throws Exception {
        File toDir = new File("target/tmp");
        delete(toDir);
        manager.download(
                ImmutableList.<ModuleDescriptor>of(new ModuleDescriptor("com.github.kevinsawicki:http-request:0.1", "shell", "")),
                toDir,
                Collections.<String>emptyList());

        assertThat(toDir.list()).containsExactly("http-request.jar");
        delete(toDir);
    }

    @Test
    public void should_download_module_and_dependencies() throws Exception {
        File toDir = new File("target/tmp");
        delete(toDir);
        manager.download(
                ImmutableList.<ModuleDescriptor>of(new ModuleDescriptor("commons-httpclient:commons-httpclient:2.0", "shell", "")),
                toDir,
                Collections.<String>emptyList());

        assertThat(toDir.list()).containsExactly("commons-httpclient.jar", "commons-lang.jar", "commons-logging.jar");
        delete(toDir);
    }

    @Test
    public void should_download_module_excluding_some_dependencies() throws Exception {
        File toDir = new File("target/tmp");
        delete(toDir);
        manager.download(
                ImmutableList.<ModuleDescriptor>of(new ModuleDescriptor("commons-httpclient:commons-httpclient:2.0", "shell", "")),
                toDir,
                Arrays.asList("commons-logging:commons-logging"));

        assertThat(toDir.list()).containsExactly("commons-httpclient.jar", "commons-lang.jar");
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
