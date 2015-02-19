package restx.common.watch;

import com.google.common.eventbus.EventBus;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.*;

/**
* User: xavierhanin
* Date: 7/27/13
* Time: 2:17 PM
*/
public class StdWatcherService implements WatcherService {
    @Override
    public Closeable watch(EventBus eventBus, ExecutorService executor, Path dir, WatcherSettings settings) {
        try {
            final WatchDir watchDir = new WatchDir(eventBus, dir, settings);
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    watchDir.processEvents();
                }
            });
            return watchDir;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    private static class WatchDir implements Closeable {
        private final WatchService watcher;
        private final Map<WatchKey,Path> keys;
        private final boolean recursive;
        private final EventCoalescor<FileWatchEvent> coalescor;
        private final Path root;
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
                        throws IOException {
                    register(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }

        /**
         * Creates a WatchService and registers the given directory
         */
        WatchDir(EventBus eventBus, Path dir, WatcherSettings settings) throws IOException {
            this.watcher = FileSystems.getDefault().newWatchService();
            this.keys = new HashMap<>();
            this.recursive = settings.recurse();
            this.root = dir;
            this.coalescor = FileWatchEventCoalescor.create(eventBus, settings.coalescePeriod());

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

                    coalescor.post(FileWatchEvent.newInstance(root, dir, ev.context(), ev.kind(), ev.count()));

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

        @Override
        public void close() throws IOException {
            watcher.close();
            coalescor.close();
        }
    }
}
