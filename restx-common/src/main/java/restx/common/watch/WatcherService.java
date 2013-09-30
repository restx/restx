package restx.common.watch;

import com.google.common.eventbus.EventBus;

import java.io.Closeable;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;

/**
* User: xavierhanin
* Date: 7/27/13
* Time: 2:17 PM
*/
public interface WatcherService {
    Closeable watch(EventBus eventBus, ExecutorService executor, Path dir, WatcherSettings watcherSettings);
    boolean isEnabled();
}
