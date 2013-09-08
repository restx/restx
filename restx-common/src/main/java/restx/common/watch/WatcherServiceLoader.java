package restx.common.watch;

import java.util.ServiceLoader;

/**
 * User: xavierhanin
 * Date: 7/27/13
 * Time: 2:15 PM
 */
public class WatcherServiceLoader {
    private static final WatcherService WATCHER_SERVICE;

    static {
        WatcherService watcher = null;
        ServiceLoader<WatcherService> watcherServices = ServiceLoader.load(WatcherService.class);
        for (WatcherService watcherService : watcherServices) {
            if (watcherService.isEnabled()) {
                watcher = watcherService;
                break;
            }
        }

        if (watcher == null) {
            watcher = new StdWatcherService();
            String osName = System.getProperty("os.name");
            if (osName.startsWith("Mac OS X") || osName.startsWith("Darwin")) {
                System.out.println("WARN: using default watch service on MacOSX uses polling.\n" +
                        "Add `restx-barbarywatch` to your classpath to have real time file system notifications.");
            }
        }

        WATCHER_SERVICE = watcher;
    }

    public static WatcherService getWatcherService() {
        return WATCHER_SERVICE;
    }
}
