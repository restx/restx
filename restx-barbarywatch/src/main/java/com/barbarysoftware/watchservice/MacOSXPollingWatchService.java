package com.barbarysoftware.watchservice;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * An implementation of the Watch Service API that uses polling. This is suitable for OS X pre-Leopard.
 *
 * @author Steve McLeod
 */
class MacOSXPollingWatchService extends AbstractWatchService {

    private static final int INITIAL_DELAY = 10; //  in seconds
    private static final int DELAY = 10; // in seconds

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "WatchService Thread");
        }
    });

    MacOSXPollingWatchService() {
    }

    @Override
    WatchKey register(WatchableFile watchableFile, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifers) throws IOException {
        final File file = watchableFile.getFile();
        final Map<File, Long> lastModifiedMap = createLastModifiedMap(file);
        final MacOSXWatchKey watchKey = new MacOSXWatchKey(this, events);
        final FileTreeScanner scanner = new FileTreeScanner(watchKey, lastModifiedMap, file);

        Runnable r = new Runnable() {
            @Override
            public void run() {
                scanner.scan();
            }
        };

        executor.scheduleWithFixedDelay(r, INITIAL_DELAY, DELAY, TimeUnit.SECONDS);
        return watchKey;
    }

    private Map<File, Long> createLastModifiedMap(File file) {
        Map<File, Long> lastModifiedMap = new ConcurrentHashMap<File, Long>();
        for (File child : recursiveListFiles(file)) {
            lastModifiedMap.put(child, child.lastModified());
        }
        return lastModifiedMap;
    }

    private static Set<File> recursiveListFiles(File file) {
        Set<File> files = new HashSet<File>();
        files.add(file);
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                files.addAll(recursiveListFiles(child));
            }
        }
        return files;
    }

    @Override
    void implClose() {
        executor.shutdown();
    }

    private static class FileTreeScanner {
        private final MacOSXWatchKey watchKey;
        private final Map<File, Long> lastModifiedMap;
        private final File folder;

        private FileTreeScanner(MacOSXWatchKey watchKey, Map<File, Long> lastModifiedMap, File folder) {
            this.watchKey = watchKey;
            this.lastModifiedMap = lastModifiedMap;
            this.folder = folder;
        }

        public void scan() {
            scanFolderForChanges(folder);
        }

        private void scanFolderForChanges(File folder) {
            final Set<File> filesOnDisk = recursiveListFiles(folder);

            for (File file : findCreatedFiles(filesOnDisk)) {
                if (watchKey.isReportCreateEvents()) {
                    watchKey.signalEvent(StandardWatchEventKind.ENTRY_CREATE, file);
                }
                lastModifiedMap.put(file, file.lastModified());
            }

            for (File file : findModifiedFiles(filesOnDisk)) {
                if (watchKey.isReportModifyEvents()) {
                    watchKey.signalEvent(StandardWatchEventKind.ENTRY_MODIFY, file);
                }
                lastModifiedMap.put(file, file.lastModified());
            }

            for (File file : findDeletedFiles(filesOnDisk)) {
                if (watchKey.isReportDeleteEvents()) {
                    watchKey.signalEvent(StandardWatchEventKind.ENTRY_DELETE, file);
                }
                lastModifiedMap.remove(file);
            }
        }

        private List<File> findModifiedFiles(Set<File> filesOnDisk) {
            List<File> modifiedFileList = new ArrayList<File>();
            for (File file : filesOnDisk) {
                final Long lastModified = lastModifiedMap.get(file);
                if (lastModified != null && lastModified != file.lastModified()) {
                    modifiedFileList.add(file);
                }
            }
            return modifiedFileList;
        }

        private List<File> findCreatedFiles(Set<File> filesOnDisk) {
            List<File> createdFileList = new ArrayList<File>();
            for (File file : filesOnDisk) {
                if (!lastModifiedMap.containsKey(file)) {
                    createdFileList.add(file);
                }
            }
            return createdFileList;
        }

        private List<File> findDeletedFiles(Set<File> filesOnDisk) {
            List<File> deletedFileList = new ArrayList<File>();
            for (File file : lastModifiedMap.keySet()) {
                if (!filesOnDisk.contains(file)) {
                    deletedFileList.add(file);
                }
            }
            return deletedFileList;
        }
    }
}