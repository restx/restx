package restx.apidocs.doclet;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Date: 16/5/14
 * Time: 21:11
 */
public class ApidocsDocletTest {
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void should_generate_doc() throws Exception {
        File dir = testFolder.newFolder();
        String[] javadocargs = {
                "-d", dir.getAbsolutePath(),
                "-doclet", "restx.apidocs.doclet.ApidocsDoclet",
                "src/test/resources/test/DocletTestResource.java" };
        com.sun.tools.javadoc.Main.execute(javadocargs);

        assertThat(new File(dir, "index.html")).exists();
    }

    @Test
    public void should_generate_doc_without_standard_doclet() throws Exception {
        File dir = testFolder.newFolder();
        String[] javadocargs = {
                "-d", dir.getAbsolutePath(),
                "-doclet", "restx.apidocs.doclet.ApidocsDoclet",
                "-disable-standard-doclet",
                "src/test/resources/test/DocletTestResource.java"
        };
        com.sun.tools.javadoc.Main.execute(javadocargs);

        assertThat(new File(dir, "index.html")).doesNotExist();
    }
}
