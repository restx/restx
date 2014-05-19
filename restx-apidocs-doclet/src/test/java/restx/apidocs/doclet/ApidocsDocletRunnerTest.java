package restx.apidocs.doclet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

/**
 * Date: 16/5/14
 * Time: 21:11
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
}
