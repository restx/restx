package restx.common;

import com.google.common.base.Function;
import com.google.common.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.*;

/**
 */
public class MoreFiles {
    public static final Function<? super Path, ? extends File> pathToFile = new Function<Path, File>() {
        @Override
        public File apply(Path input) {
            return input.toFile();
        }
    };
    public static final Function<String, Path> strToPath = new Function<String, Path>() {
        @Override
        public Path apply(String input) {
            return FileSystems.getDefault().getPath(input);
        }
    };

    public static void delete(Path path) throws IOException {
        if (path.toFile().isDirectory()) {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                        throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    // try to delete the file anyway, even if its attributes
                    // could not be read, since delete-only access is
                    // theoretically possible
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (exc == null) {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    } else {
                        // directory iteration failed; propagate exception
                        throw exc;
                    }
                }
            });
        } else {
            Files.deleteIfExists(path);
        }
    }

    public static void watch(final Path dir, final EventBus eventBus, ExecutorService executor) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    new WatchDir(eventBus, dir, true).processEvents();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public static class FileWatchEvent {
        private final Path dir;
        private final Path path;
        private final WatchEvent.Kind kind;
        private final int count;

        public FileWatchEvent(Path dir, Path path, WatchEvent.Kind kind, int count) {
            this.dir = dir;
            this.path = path;
            this.kind = kind;
            this.count = count;
        }

        public Path getDir() {
            return dir;
        }

        public Path getPath() {
            return path;
        }

        public WatchEvent.Kind getKind() {
            return kind;
        }

        public int getCount() {
            return count;
        }

        @Override
        public String toString() {
            return "FileWatchEvent{" +
                    "dir=" + dir +
                    ", path=" + path +
                    ", kind=" + kind +
                    ", count=" + count +
                    '}';
        }
    }

    private static class WatchDir {
        private final WatchService watcher;
        private final Map<WatchKey,Path> keys;
        private final boolean recursive;
        private final EventBus eventBus;
        private boolean trace = false;

        @SuppressWarnings("unchecked")
        static <T> WatchEvent<T> cast(WatchEvent<?> event) {
            return (WatchEvent<T>)event;
        }

        /**
         * Register the given directory with the WatchService
         */
        private void register(Path dir) throws IOException {
            WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            if (trace) {
                Path prev = keys.get(key);
                if (prev == null) {
                    System.out.format("register: %s\n", dir);
                } else {
                    if (!dir.equals(prev)) {
                        System.out.format("update: %s -> %s\n", prev, dir);
                    }
                }
            }
            keys.put(key, dir);
        }

        /**
         * Register the given directory, and all its sub-directories, with the
         * WatchService.
         */
        private void registerAll(final Path start) throws IOException {
            // register directory and sub-directories
            Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException
                {
                    register(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }

        /**
         * Creates a WatchService and registers the given directory
         */
        WatchDir(EventBus eventBus, Path dir, boolean recursive) throws IOException {
            this.watcher = FileSystems.getDefault().newWatchService();
            this.keys = new HashMap<>();
            this.recursive = recursive;
            this.eventBus = eventBus;

            if (recursive) {
                registerAll(dir);
            } else {
                register(dir);
            }
        }

        /**
         * Process all events for keys queued to the watcher
         */
        void processEvents() {
            for (;;) {

                // wait for key to be signalled
                WatchKey key;
                try {
                    key = watcher.take();
                } catch (InterruptedException x) {
                    return;
                }

                Path dir = keys.get(key);
                if (dir == null) {
                    System.err.println("WatchKey not recognized!!");
                    continue;
                }

                for (WatchEvent<?> event: key.pollEvents()) {
                    WatchEvent.Kind kind = event.kind();

                    // Context for directory entry event is the file name of entry
                    WatchEvent<Path> ev = cast(event);

                    eventBus.post(new FileWatchEvent(dir, ev.context(), ev.kind(), ev.count()));

                    if (kind == OVERFLOW) {
                        continue;
                    }

                    Path name = ev.context();
                    Path child = dir.resolve(name);

                    // if directory is created, and watching recursively, then
                    // register it and its sub-directories
                    if (recursive && (kind == ENTRY_CREATE)) {
                        try {
                            if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                                registerAll(child);
                            }
                        } catch (IOException x) {
                            // ignore to keep sample readbale
                        }
                    }
                }

                // reset key and remove from set if directory no longer accessible
                boolean valid = key.reset();
                if (!valid) {
                    keys.remove(key);

                    // all directories are inaccessible
                    if (keys.isEmpty()) {
                        break;
                    }
                }
            }
        }
    }
}
