package restx.kt.compiler;

import javax.tools.Diagnostic;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by xhanin on 09/02/2018.
 */
public class KotlinCompiler {
    private final Path destination;

    public KotlinCompiler(Path destination) {
        this.destination = destination;
    }

    public Collection<Diagnostic<?>> compile(Collection<Path> sources) {
        for (Path source : sources) {
            try {
                KotlinCompilerHelper.INSTANCE.compileKotlinScript(
                        Thread.currentThread().getContextClassLoader(),
                        source.toUri().toURL(),
                        destination,
                        (state, d) -> true
                        );
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("can't convert path to URL: " + e);
            }
        }

        return Collections.emptyList();
    }
}
