package restx.apidocs.doclet;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Paths;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test is located in core because we need to have core classes in classpath to run the doclet, but the core module
 * need to depend on apidocs-doclet module to generate doclet automatically.
 */
public class ApidocsDocletRunnerTest {
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void should_generate_notes() throws Exception {
        File target = testFolder.newFolder();

        new ApidocsDocletRunner()
                .setTargetDir(target.toPath())
                .addSources(Paths.get("src/test/resources/test/DocletTestResource.java"))
                .run();

        assertThat(new File(target, "apidocs/test.DocletTestResource.notes.json")).exists();
    }

    @Test
    public void should_generate_notes_with_collection() throws Exception {
        File target = testFolder.newFolder();

        new ApidocsDocletRunner()
                .setTargetDir(target.toPath())
                .addSources(asList(Paths.get("src/test/resources/test/DocletTestResource.java")))
                .run();

        assertThat(new File(target, "apidocs/test.DocletTestResource.notes.json")).exists();
    }
}
