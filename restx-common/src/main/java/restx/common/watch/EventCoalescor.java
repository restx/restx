package restx.common.watch;

import com.google.common.eventbus.EventBus;

import java.io.Closeable;
import java.io.IOException;
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
public abstract class EventCoalescor<T> implements Closeable {

	/**
	 * Create an instance of an {@link EventCoalescor} which accept all kind of events,
	 * as it is untyped.
	 *
	 * @param eventBus the event bus where to post processed events
	 * @param coalescePeriod the coalesce period
	 * @return the generic event coalescor
	 */
	public static EventCoalescor<Object> generic(EventBus eventBus, long coalescePeriod) {
		return new GenericEventCoalescor(eventBus, coalescePeriod);
	}

    final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    final EventBus eventBus;
    final long coalescePeriod;

    EventCoalescor(EventBus eventBus, long coalescePeriod) {
        this.eventBus = eventBus;
        this.coalescePeriod = coalescePeriod;
    }

	public abstract void post(final T event);

    @Override
    public void close() throws IOException {
        executor.shutdownNow();
    }

	/**
	 * generic coalescor, using untyped events
	 */
	private static class GenericEventCoalescor extends EventCoalescor<Object> {
		private final Set<Object> queue = new LinkedHashSet<>();

		private GenericEventCoalescor(EventBus eventBus, long coalescePeriod) {
			super(eventBus, coalescePeriod);
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
}
