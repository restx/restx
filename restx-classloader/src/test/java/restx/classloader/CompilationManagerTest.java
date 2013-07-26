package restx.classloader;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.assertj.core.util.Files;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * User: xavierhanin
 * Date: 7/26/13
 * Time: 4:59 PM
 */
public class CompilationManagerTest {
    FileSystem fileSystem = FileSystems.getDefault();
    Path testSourceRoot = fileSystem.getPath("src/test/test-classes-sources");
    Path sourceRoot = fileSystem.getPath("tmp/src");
    List<Path> sourceRoots = asList(sourceRoot);
    Path destination = fileSystem.getPath("tmp/classes");
    EventBus eventBus = new EventBus();
    Collection<CompilationFinishedEvent> events = new ArrayList<>();

    @Before
    public void setup() {
        Files.delete(destination.toFile());
        eventBus.register(new Object() {
            @Subscribe
            public void onCompilationFinished(CompilationFinishedEvent event) {
                events.add(event);
            }
        });
    }

    @Test
    public void should_rebuild_build() throws Exception {
        CompilationManager compilationManager = new CompilationManager(eventBus, sourceRoots, destination);

        assertThat(compilationManager.getClassFile("restx.classloader.TestSimpleClass").isPresent()).isFalse();

        prepareSource("restx/classloader/TestSimpleClass.java");
        compilationManager.rebuild();
        assertThat(compilationManager.getClassFile("restx.classloader.TestSimpleClass").isPresent()).isTrue();
        assertThat(events).hasSize(1);

        // rebuild should trigger a new compilation
        compilationManager.rebuild();
        assertThat(events).hasSize(2);
    }

    @Test
    public void should_incremental_build() throws Exception {
        CompilationManager compilationManager = new CompilationManager(eventBus, sourceRoots, destination);

        assertThat(compilationManager.getClassFile("restx.classloader.TestSimpleClass").isPresent()).isFalse();

        prepareSource("restx/classloader/TestSimpleClass.java");
        compilationManager.incrementalCompile();
        assertThat(compilationManager.getClassFile("restx.classloader.TestSimpleClass").isPresent()).isTrue();
        assertThat(events).hasSize(1);

        // incremental compile should do nothing
        compilationManager.incrementalCompile();
        assertThat(compilationManager.getClassFile("restx.classloader.TestSimpleClass").isPresent()).isTrue();
        assertThat(events).hasSize(1);
    }


    private void prepareSource(String path) throws IOException {
        File to = sourceRoot.resolve(path).toFile();
        to.getParentFile().mkdirs();
        com.google.common.io.Files.copy(
                testSourceRoot.resolve(path).toFile(),
                to);
    }
}
