package com.barbarysoftware.watchservice;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;

import java.io.File;

import static com.barbarysoftware.watchservice.StandardWatchEventKind.ENTRY_CREATE;
import static com.barbarysoftware.watchservice.StandardWatchEventKind.ENTRY_DELETE;
import static com.barbarysoftware.watchservice.StandardWatchEventKind.ENTRY_MODIFY;

public class WatchServiceTest {
    @Before
    public void check() {
        Assume.assumeTrue(isEnabled());
    }

    @org.junit.Test
    public void testNewWatchService() throws Exception {
        Assert.assertNotNull(WatchService.newWatchService());
    }

    @org.junit.Test
    public void testWatchingInvalidFolder() throws Exception {
        final WatchService watcher = WatchService.newWatchService();
        WatchableFile f = new WatchableFile(new File("/thisfolderdoesntexist"));
        f.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
    }

    @org.junit.Test
    public void testNonsensePath() throws Exception {
        final WatchService watcher = WatchService.newWatchService();
        WatchableFile f = new WatchableFile(new File("/path/to/watch"));
        f.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
    }

    @org.junit.Test(expected = NullPointerException.class)
    public void testWatchingNull() throws Exception {
        new WatchableFile(null);
    }

    @org.junit.Test
    public void testWatchingFile() throws Exception {
        final WatchService watcher = WatchService.newWatchService();
        WatchableFile f = new WatchableFile(File.createTempFile("watcher_", null));
        f.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
    }

    public boolean isEnabled() {
        String osName = System.getProperty("os.name");
        return osName.startsWith("Mac OS X") || osName.startsWith("Darwin");
    }

}
