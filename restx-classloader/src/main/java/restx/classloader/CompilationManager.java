package restx.classloader;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.common.MoreFiles;
import restx.common.watch.FileWatchEvent;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.concurrent.*;

import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Iterables.transform;
import static java.util.Collections.singleton;

/**
 * A compilation manager is responsible for compiling a set of source roots into a
 * destination directory.
 *
 * It is able to scan for changes and compile only modified files, and also watch for changes
 * to automatically compile on changes.
 *
 * It also trigger events whenever a compilation ends.
 */
public class CompilationManager {
    private static final Runnable NO_OP = new Runnable() {
        @Override
        public void run() {
        }
    };

    private static final Logger logger = LoggerFactory.getLogger(CompilationManager.class);
    private final EventBus eventBus;

    private final JavaCompiler javaCompiler;
    private final DiagnosticCollector<JavaFileObject> diagnostics;

    private final Iterable<Path> sourceRoots;
    private final Path destination;

    private final StandardJavaFileManager fileManager;

    // compile executor is a single thread, we can't perform compilations concurrently
    // all compilation will be done by the compile executor thread
    private final ScheduledExecutorService compileExecutor = Executors.newSingleThreadScheduledExecutor();

    private final ConcurrentLinkedDeque<Path> compileQueue = new ConcurrentLinkedDeque<>();

    private ExecutorService watcherExecutor;

    private volatile boolean compiling;

    // these parameters should be overridable, at least with system properties
    private final long compilationTimeout = 60; // in seconds
    private final int autoCompileQuietPeriod = 50; // ms
    private final boolean useLastModifiedTocheckChanges = true;

    public CompilationManager(EventBus eventBus, Iterable<Path> sourceRoots, Path destination) {
        this.eventBus = eventBus;
        this.sourceRoots = sourceRoots;
        this.destination = destination;

        diagnostics = new DiagnosticCollector<>();
        javaCompiler = ToolProvider.getSystemJavaCompiler();
        fileManager = javaCompiler.getStandardFileManager(diagnostics, Locale.ENGLISH, Charsets.UTF_8);
        try {
            if (!destination.toFile().exists()) {
                destination.toFile().mkdirs();
            }

            fileManager.setLocation(StandardLocation.SOURCE_PATH, transform(sourceRoots, MoreFiles.pathToFile));
            fileManager.setLocation(StandardLocation.CLASS_OUTPUT, singleton(destination.toFile()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        eventBus.register(new Object() {
            @Subscribe
            public void onWatchEvent(FileWatchEvent event) {
                WatchEvent.Kind<Path> kind = event.getKind();
                Path source = event.getDir().resolve(event.getPath());
                if (!source.toFile().isFile()) {
                    return;
                }
                if (kind == StandardWatchEventKinds.ENTRY_MODIFY
                        || kind == StandardWatchEventKinds.ENTRY_CREATE) {
                    if (!queueCompile(source)) {
                        rebuild();
                    }
                } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                    rebuild();
                } else {
                    rebuild();
                }
            }
        });
    }

    private boolean queueCompile(final Path source) {
        boolean b = compileQueue.offerLast(source);
        if (!b) {
            return false;
        }
        compileExecutor.schedule(new Runnable() {
            @Override
            public void run() {
                // nothing added since submission, quiet period is over
                if (compileQueue.getLast() == source) {
                    Collection<Path> sources = new HashSet<>();
                    while (!compileQueue.isEmpty()) {
                        sources.add(compileQueue.removeFirst());
                    }
                    compile(sources);
                }
            }
        }, autoCompileQuietPeriod, TimeUnit.MILLISECONDS);
        return true;
    }


    /**
     * Returns the path of the .class file containing bytecode for the given class (by name).
     *
     * @param className the class for which the class file should be returned
     * @return the Path of the class file, absent if it doesn't exists.
     */
    public Optional<Path> getClassFile(String className) {
        Path classFilePath = destination.resolve(className.replace('.', '/') + ".class");
        if (classFilePath.toFile().exists()) {
            return Optional.of(classFilePath);
        } else {
            return Optional.absent();
        }
    }

    public void startAutoCompile() {
        synchronized (this) {
            if (watcherExecutor == null) {
                watcherExecutor = Executors.newCachedThreadPool();
                for (Path sourceRoot : sourceRoots) {
                    MoreFiles.watch(sourceRoot, eventBus, watcherExecutor);
                }
            }
        }
    }

    public void stopAutoCompile() {
        synchronized (this) {
            if (watcherExecutor != null) {
                watcherExecutor.shutdownNow();
                watcherExecutor = null;
            }
        }
    }

    public void awaitAutoCompile() {
        try {
            if (compileQueue.isEmpty()) {
                // nothing in compile queue, we wait for current compilation if any by submitting a noop task
                // and waiting for it
                compileExecutor.submit(NO_OP).get(compilationTimeout, TimeUnit.SECONDS);
            } else {
                // we are in quiet period, let's submit a task after the quiet period and for it
                // if more file changes occur during that period we may miss them, but the purpose of this method
                // is to wait for autoCompile triggered *before* the call.
                compileExecutor.schedule(
                            NO_OP, autoCompileQuietPeriod + 10, TimeUnit.MILLISECONDS)
                        .get(compilationTimeout, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Performs an incremantal compilation.
     */
    public void incrementalCompile() {
        try {
            Exception e = compileExecutor.submit(new Callable<Exception>() {
                @Override
                public Exception call() throws Exception {
                    try {
                        final Collection<Path> sources = new ArrayList<>();
                        for (Path sourceRoot : sourceRoots) {
                            if (sourceRoot.toFile().exists()) {
                                Files.walkFileTree(sourceRoot, new SimpleFileVisitor<Path>() {
                                    @Override
                                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                        if (file.toString().endsWith(".java")
                                                && hasSourceChanged(file)) {
                                            sources.add(file);
                                        }
                                        return FileVisitResult.CONTINUE;
                                    }
                                });
                            }
                        }
                        compile(sources);
                        return null;
                    } catch (Exception e) {
                        return e;
                    }
                }
            }).get(compilationTimeout, TimeUnit.SECONDS);
            if (e != null) {
                throw new RuntimeException(e);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Clean destination and do a full build.
     */
    public void rebuild() {
        try {
            Exception e = compileExecutor.submit(new Callable<Exception>() {
                @Override
                public Exception call() throws Exception {
                    try {
                        compileQueue.clear();
                        MoreFiles.delete(destination);
                        destination.toFile().mkdirs();


                        final Collection<Path> sources = new ArrayList<>();
                        for (Path sourceRoot : sourceRoots) {
                            if (sourceRoot.toFile().exists()) {
                                Files.walkFileTree(sourceRoot, new SimpleFileVisitor<Path>() {
                                    @Override
                                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                                            throws IOException {
                                        if (file.toString().endsWith(".java")) {
                                            sources.add(file);
                                        }
                                        return FileVisitResult.CONTINUE;
                                    }
                                });
                            }
                        }

                        compile(sources);
                        return null;
                    } catch (Exception e) {
                        return e;
                    }
                }
            }).get(compilationTimeout, TimeUnit.SECONDS);
            if (e != null) {
                throw new RuntimeException(e);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    private void compile(Collection<Path> sources) {
        // MUST BE CALLED in compileExecutor only
        Stopwatch stopwatch = new Stopwatch().start();
        compiling = true;
        try {
            Iterable<? extends JavaFileObject> javaFileObjects =
                    fileManager.getJavaFileObjectsFromFiles(transform(sources, MoreFiles.pathToFile));

            if (isEmpty(javaFileObjects)) {
                logger.info("compilation finished: up to date");
                return;
            }
            JavaCompiler.CompilationTask compilationTask = javaCompiler.getTask(
                    null, fileManager, diagnostics, null, null, javaFileObjects);

            boolean valid = compilationTask.call();
            if (valid) {
                for (Path source : sources) {
                    File hashFile = hashForSource(source).toFile();
                    if (!hashFile.getParentFile().exists()) {
                        hashFile.getParentFile().mkdirs();
                    }
                    com.google.common.io.Files.write(
                            com.google.common.io.Files.hash(source.toFile(), Hashing.md5()).toString(),
                            hashFile, Charsets.UTF_8);
                }


                logger.info("compilation finished: {} sources compiled in {}", sources.size(), stopwatch.stop());
                eventBus.post(new CompilationFinishedEvent(this, DateTime.now()));
                for (Diagnostic<?> d : diagnostics.getDiagnostics()) {
                    logger.debug("{}", d);
                }
            } else {
                StringBuilder sb = new StringBuilder();
                for (Diagnostic<?> d : diagnostics.getDiagnostics()) {
                    sb.append(d).append("\n");
                }
                throw new RuntimeException("Compilation failed:\n" + sb);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            compiling = false;
        }
    }

    /**
     * @return true if this compilation manager is currently performing a compilation task.
     */
    public boolean isCompiling() {
        return compiling;
    }

    private boolean hasSourceChanged(Path source) {
        try {
            File hashFile = hashForSource(source).toFile();
            if (!hashFile.exists()) {
                return true;
            }
            if (useLastModifiedTocheckChanges) {
                if (hashFile.lastModified() >= source.toFile().lastModified()) {
                    return false;
                }
            }

            HashCode hashCode = com.google.common.io.Files.hash(source.toFile(), Hashing.md5());
            if (com.google.common.io.Files.toString(hashFile, Charsets.UTF_8).equals(hashCode.toString())) {
                return false;
            }
        } catch (IOException e) {
            return true;
        }
        return true;
    }

    private Path hashForSource(Path source) {
        return destination.resolve(source + ".md5");
    }



}
