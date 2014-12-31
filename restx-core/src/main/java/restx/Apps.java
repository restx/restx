package restx;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.base.StandardSystemProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.io.ByteStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.apidocs.doclet.ApidocsDocletRunner;
import restx.classloader.CompilationFinishedEvent;
import restx.classloader.CompilationManager;
import restx.classloader.CompilationSettings;
import restx.common.MoreFiles;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import static com.google.common.collect.Iterables.transform;

/**
 * User: xavierhanin
 * Date: 7/28/13
 * Time: 8:16 AM
 */
public class Apps {
    private static final Logger logger = LoggerFactory.getLogger(Apps.class);
    private static final String PATH_SEP = StandardSystemProperty.PATH_SEPARATOR.value();

    public static Apps with(AppSettings appSettings) {
        return new Apps(appSettings);
    }

    private final AppSettings settings;

    public Apps(AppSettings settings) {
        this.settings = settings;
    }

    public CompilationManager newAppCompilationManager(EventBus eventBus, CompilationSettings compilationSettings) {
        if (!hasSystemJavaCompiler()) {
            throw new IllegalStateException(
                    "trying to setup a compilation manager while no system compiler is available. " +
                            "This should be prevented by checking the system java compiler first. " +
                            "Use hasSystemJavaCompiler() to check that before calling this method.");
        }
        if (hasJavadocTools()) {
            eventBus.register(new Object() {
                @Subscribe
                public void onCompilationFinished(
                        CompilationFinishedEvent event) {
                    new ApidocsDocletRunner()
                            .setTargetDir(getTargetClasses())
                            .addSources(event.getSources())
                            .run();
                    ;
                }
            });
        } else {
            logger.info("can't enable Apidocs doclet: make sure tools.jar is in your classpath");
        }
        return new CompilationManager(eventBus, getSourceRoots(), getTargetClasses(), compilationSettings);
    }

    public Path getTargetClasses() {
        return FileSystems.getDefault().getPath(settings.targetClasses());
    }

    public Iterable<Path> getSourceRoots() {
        return transform(Splitter.on(',').trimResults().split(settings.sourceRoots()),
                    MoreFiles.strToPath);
    }

    public boolean sourcesAvailableIn(Path basePath) {
        for(Path sourceRoot : getSourceRoots()){
            if(Files.notExists(basePath.resolve(sourceRoot))){
                return false;
            }
        }
        return true;
    }

    public Optional<String> guessAppBasePackage(Path fromDir) {
        for (Path sourceRoot : getSourceRoots()) {
            Path sourceRootDir = fromDir.resolve(sourceRoot);

            try {
                final Path[] appServer = new Path[1];
                Files.walkFileTree(sourceRootDir, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (file.getFileName().toString().equals("AppServer.java")) {
                            appServer[0] = file;
                            return FileVisitResult.TERMINATE;
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
                if (appServer[0] != null) {
                    return Optional.of(sourceRootDir.relativize(appServer[0]).getParent().toString().replace("/", "."));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return Optional.absent();
    }


    public Process run(File workingDirectory, Path targetClasses, Path dependenciesDir, List<String> vmOptions,
                              String mainClassName, List<String> args, boolean quiet) throws IOException {
        String classpath = targetClasses.toString() + PATH_SEP + dependenciesDir.toString() + "/*";
        File toolsJar = new File(System.getenv("JAVA_HOME") + "/lib/tools.jar");
        if (toolsJar.exists()) {
            classpath += PATH_SEP + toolsJar.getAbsolutePath();
        }

        final Process process = new ProcessBuilder(
                ImmutableList.<String>builder()
                        .add("java",
                                "-cp",
                                classpath)
                        .addAll(vmOptions)
                        .add(mainClassName)
                        .addAll(args)
                        .build()
        )
                .redirectErrorStream(true)
                .redirectOutput(quiet ? ProcessBuilder.Redirect.PIPE : ProcessBuilder.Redirect.INHERIT)
                .directory(workingDirectory.getAbsoluteFile())
                .start();

        if (quiet) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ByteStreams.copy(process.getInputStream(), ByteStreams.nullOutputStream());
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }).start();
        }
        return process;
    }

    public static boolean hasJavadocTools() {
        try {
            Class.forName("com.sun.tools.javadoc.Main");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean hasSystemJavaCompiler() {
        try {
            Class<?> tp = Class.forName("javax.tools.ToolProvider");
            return tp.getMethod("getSystemJavaCompiler").invoke(null) != null;
        } catch (Exception e) {
            return false;
        }
    }

}
