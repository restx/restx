package restx;

import com.google.common.base.Splitter;
import com.google.common.eventbus.EventBus;
import restx.classloader.CompilationManager;
import restx.common.MoreFiles;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import static com.google.common.collect.Iterables.transform;

/**
 * User: xavierhanin
 * Date: 7/28/13
 * Time: 8:16 AM
 */
public class Apps {
    public static CompilationManager newAppCompilationManager(EventBus eventBus) {
        return new CompilationManager(eventBus, getSourceRoots(), getTargetClasses());
    }

    public static Path getTargetClasses() {
        return FileSystems.getDefault().getPath(System.getProperty("restx.targetClasses", "tmp/classes"));
    }

    public static Iterable<Path> getSourceRoots() {
        return transform(Splitter.on(',').trimResults().split(
                    System.getProperty("restx.sourceRoots",
                            "src/main/java, src/main/resources")),
                    MoreFiles.strToPath);
    }


    public static Process run(File workingDirectory, Path targetClasses, Path dependenciesDir, String mainClassName) throws IOException {
        return new ProcessBuilder(
                "java",
                "-cp",
                targetClasses.toString() + ":" + dependenciesDir.toString() + "/*",
                mainClassName
        )
                .redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .directory(workingDirectory.getAbsoluteFile())
                .start();
    }
}
