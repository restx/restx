package restx.common;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static restx.common.MoreFiles.extractZip;

/**
 * User: xavierhanin
 * Date: 9/9/13
 * Time: 9:17 PM
 */
public class MoreFilesTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void should_copy_directories() throws Exception {
        File source = folder.newFolder("source");
        Files.write("test", new File(source, "test"), Charsets.UTF_8);
        File subfolder = new File(source, "subfolder");
        subfolder.mkdirs();
        Files.write("test2", new File(subfolder, "test2"), Charsets.UTF_8);

        Path targetDir = folder.getRoot().toPath().resolve("target");
        MoreFiles.copyDir(source.toPath(), targetDir);

        assertThat(targetDir.toFile()).exists();
        assertThat(targetDir.resolve("test").toFile()).exists().hasContent("test");
        assertThat(targetDir.resolve("subfolder/test2").toFile()).exists().hasContent("test2");
    }

    @Test
    public void should_extract_zip() throws Exception {
        File dest = folder.newFolder("dest");

        extractZip(new File("src/test/resources/restx/common/test.zip"), dest);

        assertThat(new File(dest, "A.txt")).exists();
        assertThat(new File(dest, "1/B.txt")).exists();
        assertThat(new File(dest, "1/C.txt")).exists();
        assertThat(new File(dest, "2/D.txt")).exists();
        assertThat(new File(dest, "2/E.txt")).exists().hasContent("hello");
    }
}
