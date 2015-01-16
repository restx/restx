package restx.classloader;

import com.google.common.base.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.hash.Hashing;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.common.MoreFiles;
import restx.common.watch.FileWatchEvent;
import restx.common.watch.WatcherSettings;

import javax.tools.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Iterables.transform;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.unmodifiableCollection;

/**
 * A compilation manager is responsible for compiling a set of source roots into a
 * destination directory.
 *
 * It is able to scan for changes and compile only modified files, and also watch for changes
 * to automatically compile on changes.
 *
 * It also trigger events whenever a compilation ends.
 */
public class CompilationManager implements Closeable {
    private static final Runnable NO_OP = new Runnable() {
        @Override
        public void run() {
        }
    };
    public static final Predicate<Path> DEFAULT_CLASSPATH_RESOURCE_FILTER = new Predicate<Path>() {
        @Override
        public boolean apply(java.nio.file.Path path) {
            return
                    // Intellij IDEA temporary files
                       !path.toString().endsWith("___jb_old___")
                    && !path.toString().endsWith("___jb_bak___")

                    // svn
                    && path.toAbsolutePath().toString().replace('\\', '/').indexOf("/.svn/") == -1;
        }
    };
    public static final CompilationSettings DEFAULT_SETTINGS = new CompilationSettings() {
        @Override
        public int autoCompileCoalescePeriod() {
            return 50;
        }

        @Override
        public Predicate<Path> classpathResourceFilter() {
            return DEFAULT_CLASSPATH_RESOURCE_FILTER;
        }
    };

    private static final Logger logger = LoggerFactory.getLogger(CompilationManager.class);
    private final EventBus eventBus;
    private final Predicate<Path> classpathResourceFilter;

    private final JavaCompiler javaCompiler;

    private final Iterable<Path> sourceRoots;
    private final Path destination;
    private final CompilationSettings settings;

    private final StandardJavaFileManager fileManager;

    // compile executor is a single thread, we can't perform compilations concurrently
    // all compilation will be done by the compile executor thread
    private final ScheduledExecutorService compileExecutor = Executors.newSingleThreadScheduledExecutor();

    private final ConcurrentLinkedDeque<Path> compileQueue = new ConcurrentLinkedDeque<>();

    private final Map<Path, SourceHash> hashes = new HashMap<>();

    private ExecutorService watcherExecutor;
    private final List<Closeable> closeableWatchers = new ArrayList<>();

    private volatile boolean compiling;

    // these parameters should be overridable, at least with system properties
    private final long compilationTimeout = 60; // in seconds
    private final int autoCompileQuietPeriod = 50; // ms
    private final boolean useLastModifiedTocheckChanges = true;
    private Collection<Diagnostic<?>> lastDiagnostics = new CopyOnWriteArrayList<>();

    public CompilationManager(EventBus eventBus, Iterable<Path> sourceRoots, Path destination) {
        this(eventBus, sourceRoots, destination, DEFAULT_SETTINGS);
    }

    public CompilationManager(EventBus eventBus, final Iterable<Path> sourceRoots, Path destination, CompilationSettings settings) {
        this.eventBus = checkNotNull(eventBus);
        this.sourceRoots = checkNotNull(sourceRoots);
        this.destination = checkNotNull(destination);
        this.classpathResourceFilter = checkNotNull(settings.classpathResourceFilter());
        this.settings = settings;

        javaCompiler = ToolProvider.getSystemJavaCompiler();
        if (javaCompiler == null) {
            throw new IllegalStateException(
                    "trying to setup a compilation manager while no system compiler is available." +
                            " This should be prevented by checking the system java compiler first.");
        }
        fileManager = javaCompiler.getStandardFileManager(new DiagnosticCollector<JavaFileObject>(), Locale.ENGLISH, Charsets.UTF_8);
        try {
            if (!destination.toFile().exists()) {
                destination.toFile().mkdirs();
            }

            fileManager.setLocation(StandardLocation.SOURCE_PATH, transform(sourceRoots, MoreFiles.pathToFile));
            fileManager.setLocation(StandardLocation.CLASS_OUTPUT, singleton(destination.toFile()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        loadHashes();

        eventBus.register(new Object() {
            @Subscribe
            public void onWatchEvent(FileWatchEvent event) {
                WatchEvent.Kind<?> kind = event.getKind();
                Path source = event.getDir().resolve(event.getPath());
                if (!source.toFile().isFile()) {
                    return;
                }
                if (isSource(source)) {
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
                } else {
                    Optional<SourcePath> sourcePath = SourcePath.resolve(CompilationManager.this.sourceRoots, source);
                    if (sourcePath.isPresent()) {
                        logger.info("classpath resource updated: {}", sourcePath.get().getPath());
                        copyResource(sourcePath.get());
                    }
                }
            }
        });
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public Path getDestination() {
        return destination;
    }

    public Iterable<Path> getSourceRoots() {
        return sourceRoots;
    }

    private void copyResource(final SourcePath resourcePath) {
        compileExecutor.submit(new Runnable() {
            @Override
            public void run() {
                doCopyResource(resourcePath);
            }
        });
    }

    // IMPORTANT: this should be called in compile executor thread only
    private void doCopyResource(SourcePath resourcePath) {
        File source = resourcePath.toAbsolutePath().toFile();
        if (source.isFile() && classpathResourceFilter.apply(source.toPath())) {
            try {
                File to = destination.resolve(resourcePath.getPath()).toFile();
                to.getParentFile().mkdirs();
                boolean existed = to.exists();
                if (!existed || to.lastModified() < source.lastModified()) {
                    com.google.common.io.Files.copy(source, to);
                    ClasspathResourceEvent.Kind kind = existed ?
                            ClasspathResourceEvent.Kind.UPDATED : ClasspathResourceEvent.Kind.CREATED;
                    eventBus.post(new ClasspathResourceEvent(kind, resourcePath.getPath().toString()));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
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
                Collection<Path> watched = new ArrayList<>();
                for (Path sourceRoot : sourceRoots) {
                    if (sourceRoot.toFile().exists()) {
                        watched.add(sourceRoot);
                        closeableWatchers.add(MoreFiles.watch(sourceRoot, eventBus, watcherExecutor, new WatcherSettings() {
                            @Override
                            public int coalescePeriod() {
                                return settings.autoCompileCoalescePeriod();
                            }

                            @Override
                            public boolean recurse() {
                                return true;
                            }
                        }));
                    } else {
                        logger.info("source root {} does not exist - IGNORED", sourceRoot);
                    }
                }
                logger.info("watching for changes in {}; current location is {}",
                        watched, new File(".").getAbsoluteFile());
            }
        }
    }

    public void stopAutoCompile() {
        synchronized (this) {
            if (watcherExecutor != null) {
                watcherExecutor.shutdownNow();
                watcherExecutor = null;
            }
            if (closeableWatchers.size() > 0) {
                for (Closeable closeableWatcher : closeableWatchers) {
                    try {
                        closeableWatcher.close();
                    } catch (IOException ignored) { }
                }
                closeableWatchers.clear();
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
     * Performs an incremental compilation.
     */
    public void incrementalCompile() {
        try {
            Exception e = compileExecutor.submit(new Callable<Exception>() {
                @Override
                public Exception call() throws Exception {
                    try {
                        final Collection<Path> sources = new ArrayList<>();
                        for (final Path sourceRoot : sourceRoots) {
                            if (sourceRoot.toFile().exists()) {
                                Files.walkFileTree(sourceRoot, new SimpleFileVisitor<Path>() {
                                    @Override
                                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                        if (isSource(file)) {
                                            if (hasSourceChanged(sourceRoot, sourceRoot.relativize(file))) {
                                                sources.add(file);
                                            }
                                        } else if (file.toFile().isFile()) {
                                            doCopyResource(SourcePath.valueOf(sourceRoot, sourceRoot.relativize(file)));
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

    private boolean isSource(Path file) {
        return file.toString().endsWith(".java");
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
                        for (final Path sourceRoot : sourceRoots) {
                            if (sourceRoot.toFile().exists()) {
                                Files.walkFileTree(sourceRoot, new SimpleFileVisitor<Path>() {
                                    @Override
                                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                                            throws IOException {
                                        if (isSource(file)) {
                                            sources.add(file);
                                        } else if (file.toFile().isFile()) {
                                            doCopyResource(SourcePath.valueOf(sourceRoot, sourceRoot.relativize(file)));
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

    public void compileSources(final Path... sources) {
        try {
            Exception e = compileExecutor.submit(new Callable<Exception>() {
                @Override
                public Exception call() throws Exception {
                    compile(asList(sources));
                    return null;
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

    public Collection<Diagnostic<?>> getLastDiagnostics() {
        return unmodifiableCollection(lastDiagnostics);
    }

    private final static AtomicLong CLASSLOADER_COUNT = new AtomicLong();

    public HotReloadingClassLoader newHotReloadingClassLoader(String rootPackage, ImmutableSet<Class> coldClasses) {
        try {
            CLASSLOADER_COUNT.incrementAndGet();
            final String name = "HotCompile[" + CLASSLOADER_COUNT + "]";
            final Path destinationDir = getDestination();
            return new HotReloadingClassLoader(
                    new URLClassLoader(
                            new URL[]{destinationDir.toUri().toURL()},
                            Thread.currentThread().getContextClassLoader()),
                    rootPackage, coldClasses
            ) {
                protected InputStream getInputStream(String path) {
                    try {
                        return Files.newInputStream(destinationDir.resolve(path));
                    } catch (IOException e) {
                        return null;
                    }
                }

                @Override
                public String toString() {
                    return name;
                }
            };
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

    }

    private void compile(Collection<Path> sources) {
        // MUST BE CALLED in compileExecutor only
        Stopwatch stopwatch = Stopwatch.createStarted();
        compiling = true;
        try {
            lastDiagnostics.clear();
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            Iterable<? extends JavaFileObject> javaFileObjects =
                    fileManager.getJavaFileObjectsFromFiles(transform(sources, MoreFiles.pathToFile));

            if (isEmpty(javaFileObjects)) {
                logger.debug("compilation finished: up to date");
                return;
            }
            JavaCompiler.CompilationTask compilationTask = javaCompiler.getTask(
                    null, fileManager, diagnostics, asList("-g"), null, javaFileObjects);

            boolean valid = compilationTask.call();
            if (valid) {
                for (Path source : sources) {
                    Optional<SourcePath> sourcePath = SourcePath.resolve(sourceRoots, source);
                    if (sourcePath.isPresent()) {
                        SourceHash sourceHash = newSourceHashFor(sourcePath.get());
                        hashes.put(source.toAbsolutePath(), sourceHash);

                    }
                }

                saveHashes();

                logger.info("compilation finished: {} sources compiled in {}", sources.size(), stopwatch.stop());
                eventBus.post(new CompilationFinishedEvent(this, DateTime.now(), ImmutableList.copyOf(sources)));
                for (Diagnostic<?> d : diagnostics.getDiagnostics()) {
                    logger.debug("{}", d);
                }
            } else {
                StringBuilder sb = new StringBuilder();
                for (Diagnostic<?> d : diagnostics.getDiagnostics()) {
                    sb.append(d).append("\n");
                }
                lastDiagnostics.addAll(diagnostics.getDiagnostics());
                throw new RuntimeException("Compilation failed:\n" + sb);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            compiling = false;
        }
    }

    private void saveHashes() {
        File hashesFile = hashesFile();
        hashesFile.getParentFile().mkdirs();

        try (Writer w = com.google.common.io.Files.newWriter(hashesFile, Charsets.UTF_8)) {
            for (SourceHash sourceHash : hashes.values()) {
                w.write(sourceHash.serializeAsString());
                w.write("\n");
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadHashes() {
        File hashesFile = hashesFile();
        if (hashesFile.exists()) {
            try (BufferedReader r = com.google.common.io.Files.newReader(hashesFile, Charsets.UTF_8)) {
                String line;
                while ((line = r.readLine()) != null) {
                    SourceHash sourceHash = parse(line);
                    hashes.put(sourceHash.getSourcePath().toAbsolutePath(), sourceHash);
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private File hashesFile() {
        return destination.resolve("META-INF/.hashes").toFile();
    }

    /**
     * @return true if this compilation manager is currently performing a compilation task.
     */
    public boolean isCompiling() {
        return compiling;
    }

    private boolean hasSourceChanged(Path dir, Path source) {
        try {
            SourceHash sourceHash = hashes.get(dir.resolve(source).toAbsolutePath());
            if (sourceHash != null) {
                return sourceHash.hasChanged() != sourceHash;
            } else {
                return true;
            }
        } catch (IOException e) {
            return true;
        }
    }

    private static class SourcePath {
        public static Optional<SourcePath> resolve(Iterable<Path> sourceRoots, Path source) {
            Path dir = null;
            for (Path sourceRoot : sourceRoots) {
                if ((source.isAbsolute() && source.startsWith(sourceRoot.toAbsolutePath()))
                        || (!source.isAbsolute() && source.startsWith(sourceRoot))) {
                    dir = sourceRoot;
                    break;
                }
            }
            if (dir == null) {
                logger.warn("can't find sourceRoot for {}", source);
                return Optional.absent();
            } else {
                return Optional.of(new SourcePath(dir, source.isAbsolute() ?
                                        dir.toAbsolutePath().relativize(source) :
                                        dir.relativize(source)));
            }
        }

        public static SourcePath valueOf(Path sourceRoot, Path path) {
            return new SourcePath(sourceRoot, path);
        }

        private final Path sourceDir;
        private final Path path;

        private SourcePath(Path sourceDir, Path path) {
            this.sourceDir = sourceDir;
            this.path = path;
        }

        public Path getSourceDir() {
            return sourceDir;
        }

        public Path getPath() {
            return path;
        }

        public Path toAbsolutePath() {
            return sourceDir.resolve(path).toAbsolutePath();
        }

        @Override
        public String toString() {
            return "SourcePath{" +
                    "sourceDir=" + sourceDir +
                    ", path=" + path +
                    '}';
        }
    }

    private class SourceHash {
        private final SourcePath sourcePath;
        private final String hash;
        private final long lastModified;

        private SourceHash(SourcePath sourcePath, String hash, long lastModified) {
            this.sourcePath = sourcePath;
            this.hash = hash;
            this.lastModified = lastModified;
        }

        @Override
        public String toString() {
            return "SourceHash{" +
                    "sourcePath=" + sourcePath +
                    ", hash='" + hash + '\'' +
                    ", lastModified=" + lastModified +
                    '}';
        }

        public SourcePath getSourcePath() {
            return sourcePath;
        }

        public String getHash() {
            return hash;
        }

        public long getLastModified() {
            return lastModified;
        }

        public SourceHash hasChanged() throws IOException {
            File sourceFile = sourcePath.toAbsolutePath().toFile();
            if (useLastModifiedTocheckChanges) {
                if (lastModified < sourceFile.lastModified()) {
                    return new SourceHash(sourcePath,
                            computeHash(), sourceFile.lastModified());
                }
            } else {
                String currentHash = computeHash();
                if (!currentHash.equals(hash)) {
                    return new SourceHash(sourcePath,
                            currentHash, sourceFile.lastModified());
                }
            }
            return this;
        }

        private String computeHash() throws IOException {
            return hash(sourcePath.toAbsolutePath().toFile());
        }

        public String serializeAsString() throws IOException {
            return Joiner.on("**").join(sourcePath.getSourceDir(), sourcePath.getPath(), hash, lastModified);
        }
    }

    private SourceHash newSourceHashFor(SourcePath sourcePath) throws IOException {
        File sourceFile = sourcePath.toAbsolutePath().toFile();
        return new SourceHash(sourcePath, hash(sourceFile), sourceFile.lastModified());
    }

    private String hash(File file) throws IOException {
        return com.google.common.io.Files.hash(file, Hashing.md5()).toString();
    }

    private SourceHash parse(String str) {
        Iterator<String> parts = Splitter.on("**").split(str).iterator();
        FileSystem fileSystem = FileSystems.getDefault();
        return new SourceHash(
                SourcePath.valueOf(fileSystem.getPath(parts.next()),
                               fileSystem.getPath(parts.next())),
                parts.next(),
                Long.parseLong(parts.next())
        );
    }

    @Override
    public void close() throws IOException {
        stopAutoCompile(); // in case ato-compile was started, stop it
        compileExecutor.shutdownNow();
    }
}
