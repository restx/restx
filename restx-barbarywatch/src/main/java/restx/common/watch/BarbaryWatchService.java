package restx.common.watch;

import com.barbarysoftware.watchservice.*;
import com.barbarysoftware.watchservice.WatchKey;
import com.barbarysoftware.watchservice.WatchService;
import com.google.common.eventbus.EventBus;
import restx.common.OSUtils;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.WatchEvent;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * File watching facility based on DemoCode.
 */
public class BarbaryWatchService implements WatcherService {
    public BarbaryWatchService() {
    }

    @Override
    public Closeable watch(EventBus eventBus, ExecutorService executor,
                           Path dir, WatcherSettings watcherSettings) {
        try {
            final WatchDir watchDir = new WatchDir(eventBus, dir, watcherSettings);
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
        return OSUtils.isMacOSX();
    }

    private static class WatchDir implements Closeable{
        private final WatchService watcher;
        private final Map<WatchKey, Path> keys;
        private final boolean recursive;
        private final EventCoalescor<FileWatchEvent> coalescor;
        private final Path root;
        private boolean trace = false;

        /**
         * Register the given directory with the WatchService
         */
        private void register(Path dir) throws IOException {
            com.barbarysoftware.watchservice.WatchKey key =
                    new WatchableFile(dir.toFile()).register(watcher,
                            StandardWatchEventKind.ENTRY_CREATE, StandardWatchEventKind.ENTRY_DELETE, StandardWatchEventKind.ENTRY_MODIFY);
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
        WatchDir(EventBus eventBus, Path dir, WatcherSettings settings) throws IOException {
            this.watcher = com.barbarysoftware.watchservice.WatchService.newWatchService();
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
                com.barbarysoftware.watchservice.WatchKey key;
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

                for (com.barbarysoftware.watchservice.WatchEvent<?> event: key.pollEvents()) {
                    com.barbarysoftware.watchservice.WatchEvent.Kind<?> kind = event.kind();

                    // Context for directory entry event is the file name of entry
                    com.barbarysoftware.watchservice.WatchEvent<File> ev = asWatchEventOfFile(event);

                    WatchEvent.Kind<?> nkind = null;
                    if (ev.kind().equals(StandardWatchEventKind.ENTRY_CREATE)) {
                        nkind = StandardWatchEventKinds.ENTRY_CREATE;
                    } else if (ev.kind().equals(StandardWatchEventKind.ENTRY_DELETE)) {
                        nkind = StandardWatchEventKinds.ENTRY_DELETE;
                    } else if (ev.kind().equals(StandardWatchEventKind.ENTRY_MODIFY)) {
                        nkind = StandardWatchEventKinds.ENTRY_MODIFY;
                    } else if (ev.kind().equals(StandardWatchEventKind.OVERFLOW)) {
                        nkind = StandardWatchEventKinds.OVERFLOW;
                    }

                    coalescor.post(FileWatchEvent.newInstance(
                            root, dir, ev.context().toPath(), nkind, ev.count()));

                    if (kind == StandardWatchEventKind.OVERFLOW) {
                        continue;
                    }

                    Path name = ev.context().toPath();
                    Path child = dir.resolve(name);

                    // if directory is created, and watching recursively, then
                    // register it and its sub-directories
                    if (recursive && (kind == StandardWatchEventKind.ENTRY_CREATE)) {
                        try {
                            if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
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

        @SuppressWarnings("unchecked")
        private com.barbarysoftware.watchservice.WatchEvent<File> asWatchEventOfFile(
                com.barbarysoftware.watchservice.WatchEvent<?> event) {
            return (com.barbarysoftware.watchservice.WatchEvent<File>) event;
        }

        @Override
        public void close() throws IOException {
            watcher.close();
            coalescor.close();
        }
    }

}
