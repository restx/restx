package restx.kt.compiler;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.tools.Diagnostic;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by xhanin on 09/02/2018.
 */
public class KotlinCompilerTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void should_compile_simple_class() throws Exception {
        File target = folder.newFolder("target");

        KotlinCompiler compiler = new KotlinCompiler(target.toPath());

        Collection<Path> sources = asList(Paths.get("src/test/resources/MyClass.kt"));
        Collection<Diagnostic<?>> diagnostics = compiler.compile(sources);

        assertThat(diagnostics)
                .isNotNull()
                .isEmpty();

        assertThat(new File(target, "MyClass.class")).exists();
    }
}