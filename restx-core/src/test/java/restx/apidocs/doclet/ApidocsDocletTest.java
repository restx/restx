package restx.apidocs.doclet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

/**
 * This test is located in core because we need to have core classes in classpath to run the doclet, but the core module
 * need to depend on apidocs-doclet module to generate doclet automatically.
 */
public class ApidocsDocletTest {
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void should_generate_notes() throws Exception {
        File target = testFolder.newFolder();
        String[] javadocargs = {
                "-d", testFolder.newFolder().getAbsolutePath(),
                "-doclet", "restx.apidocs.doclet.ApidocsDoclet",
                "-restx-target-dir", target.getAbsolutePath(),
                "-disable-standard-doclet",
                "src/test/resources/test/DocletTestResource.java"
        };
        com.sun.tools.javadoc.Main.execute(javadocargs);

        assertThat(new File(target, "apidocs/test.DocletTestResource.notes.json")).exists();

        ApiEntryNotes api = new ObjectMapper().reader(ApiEntryNotes.class)
                .readValue(new File(target, "apidocs/test.DocletTestResource.notes.json"));

        assertThat(api.getName()).isEqualTo("test.DocletTestResource");
        assertThat(api.getOperations()).hasSize(1)
                .extracting("httpMethod", "path", "notes")
                .containsExactly(tuple("GET", "/test/:param1", "Test"));

        assertThat(api.getOperations().get(0).getParameters())
                .hasSize(3)
                .extracting("name", "notes")
                .containsExactly(
                        tuple("param1", "param number one"),
                        tuple("param2", "param number two"),
                        tuple("response", "my return value")
                );
    }

    @Test
    public void should_generate_standard_doc_by_default() throws Exception {
        File dir = testFolder.newFolder();
        String[] javadocargs = {
                "-d", dir.getAbsolutePath(),
                "-doclet", "restx.apidocs.doclet.ApidocsDoclet",
                "-restx-target-dir", dir.getAbsolutePath(),
                "src/test/resources/test/DocletTestResource.java" };
        com.sun.tools.javadoc.Main.execute(javadocargs);

        // should have generated standard javadoc
        assertThat(new File(dir, "index.html")).exists();
    }

    @Test
    public void should_be_able_to_disable_standard_doclet() throws Exception {
        File dir = testFolder.newFolder();
        String[] javadocargs = {
                "-d", dir.getAbsolutePath(),
                "-doclet", "restx.apidocs.doclet.ApidocsDoclet",
                "-restx-target-dir", dir.getAbsolutePath(),
                "-disable-standard-doclet",
                "src/test/resources/test/DocletTestResource.java"
        };
        com.sun.tools.javadoc.Main.execute(javadocargs);

        // should not have generated standard javadoc
        assertThat(new File(dir, "index.html")).doesNotExist();
        assertThat(new File(dir, "apidoclet.trace")).doesNotExist();
    }

    @Test
    public void should_trace_in_file_when_enabled() throws Exception {
        File dir = testFolder.newFolder();
        File target = testFolder.newFolder();
        String[] javadocargs = {
                "-d", dir.getAbsolutePath(),
                "-doclet", "restx.apidocs.doclet.ApidocsDoclet",
                "-restx-target-dir", target.getAbsolutePath(),
                "-restx-enable-trace",
                "-disable-standard-doclet",
                "src/test/resources/test/DocletTestResource.java"
        };
        com.sun.tools.javadoc.Main.execute(javadocargs);

        assertThat(new File(target, "apidoclet.trace")).exists();
    }
}
