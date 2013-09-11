package restx.common.watch;

import com.google.common.eventbus.EventBus;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Used to coalesce events occuring in a short period of time.
 *
 * <p>
 *     You submit events to the coalescor using the #post(Object) method,
 *     and if no similar (compared with #equals) event occur within the
 *     coalesce period, the event is forwarded to the underlying EventBus.
 * </p>
 * <p>
 *     Other similar events occuring within the period are simply not discarded.
 * </p>
 */
public class EventCoalescor {
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final EventBus eventBus;
    private final long coalescePeriod;

    private final Set<Object> queue = new LinkedHashSet<>();

    public EventCoalescor(EventBus eventBus, long coalescePeriod) {
        this.eventBus = eventBus;
        this.coalescePeriod = coalescePeriod;
    }

    public void post(final Object event) {
        synchronized (queue) {
            if (queue.add(event)) {
                executor.schedule(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            eventBus.post(event);
                        } finally {
                            synchronized (queue) {
                                queue.remove(event);
                            }
                        }
                    }
                }, coalescePeriod, TimeUnit.MILLISECONDS);
            }
        }
    }
}
