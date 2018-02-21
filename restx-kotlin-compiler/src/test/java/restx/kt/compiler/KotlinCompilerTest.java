package restx.kt.compiler;

import org.jetbrains.annotations.NotNull;
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

        KotlinCompiler compiler = getKotlinCompiler(target);

        compiler.compile(new Path[]{
                Paths.get("src/test/resources/simple")
                });

        assertThat(new File(target, "MyClass.class")).exists();
    }

    @Test
    public void should_compile_annotated_class() throws Exception {
        File target = folder.newFolder("target");

        KotlinCompiler compiler = getKotlinCompiler(target);

        compiler.compile(new Path[]{
                Paths.get("src/test/resources/annotated")
        });
        Collection<Path> sources = asList(Paths.get("src/test/resources/annotated"));

        assertThat(new File(target, "test/MyAnnotatedClass.class")).exists();
        assertThat(new File(target, "test/MyAnnotatedClassFactoryMachine.class")).exists();
    }

    @NotNull
    private KotlinCompiler getKotlinCompiler(File target) {
        String m2repo = System.getProperty("user.home") + "/.m2/repository";
        String kotlinVersion = "1.2.21";
        String restxVersion = "0.36-beta1-4sh";
        return new KotlinCompiler(
                Thread.currentThread().getContextClassLoader(), target.toPath(),
                Paths.get(m2repo + "/org/jetbrains/kotlin/kotlin-annotation-processing/"
                        + kotlinVersion + "/kotlin-annotation-processing-" + kotlinVersion + ".jar"),
                new Path[]{Paths.get(m2repo + "/io/restx/restx-factory-annotation-processor/"
                        + restxVersion + "/restx-factory-annotation-processor-" + restxVersion + ".jar")}
        );
    }
}