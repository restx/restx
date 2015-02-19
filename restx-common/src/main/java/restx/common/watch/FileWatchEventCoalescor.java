package restx.common.watch;

import com.google.common.eventbus.EventBus;

import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Used to coalesce {@link restx.common.watch.FileWatchEvent} in a short period of time.
 *
 * <p>
 * There is some cases where events will be discarded:
 * <ul>
 * <li>If the same event is posted multiple times, only the first occurrence will be kept.</li>
 * <li>If a create event follow a delete event, for a same file, it will be transformed into a modified event.</li>
 * </ul>
 *
 * @author apeyrard
 */
public class FileWatchEventCoalescor extends EventCoalescor<FileWatchEvent> {

	/**
	 * Create a new {@link EventCoalescor} to coalesce {@link FileWatchEvent}.
	 *
	 * @param eventBus the event bus where to post processed events
	 * @param coalescePeriod the coalesce period
	 * @return the generic event coalescor
	 */
	public static FileWatchEventCoalescor create(EventBus eventBus, long coalescePeriod) {
		return new FileWatchEventCoalescor(eventBus, coalescePeriod);
	}

	private final HashMap<FileWatchEventKey, Deque<EventReference>> queue = new HashMap<>();

	FileWatchEventCoalescor(EventBus eventBus, long coalescePeriod) {
		super(eventBus, coalescePeriod);
	}

	/**
	 * Posts a {@link restx.common.watch.FileWatchEvent}, the post will be delayed, or even discarded, if
	 * the event might be merged, with a previous one.
	 *
	 * @param event the event to try to post
	 */
	public void post(final FileWatchEvent event) {
		synchronized (queue) {
			final FileWatchEventKey key = FileWatchEventKey.fromEvent(event);

			Deque<EventReference> fileEvents;
			if ((fileEvents = queue.get(key)) == null) {
				// easy case, first event for a file, just queue it and schedule a post
				fileEvents = new ArrayDeque<>();
				queue.put(key, fileEvents);
				EventReference reference = EventReference.of(key, event);
				fileEvents.add(reference);
				schedulePost(reference);
				return;
			}

			// more complex case, we need to analyze the last saved event for this file
			EventReference last = fileEvents.getLast();
			if (!merge(last, event)) {
				// event has not been merged, so try to add it
				EventReference reference = EventReference.of(key, event);
				fileEvents.add(reference);
				schedulePost(reference);
			}
		}
	}

	/**
	 * tries to merge the current event into the current one
	 */
	private boolean merge(EventReference previous, FileWatchEvent current) {
		if (!previous.isPresent()) {
			return false;
		}

		if (previous.getReference().getKind() == current.getKind()) {
			return true; // duplicate events, keep only one
		}

		if (previous.getReference().getKind() == StandardWatchEventKinds.ENTRY_DELETE) {
			if (current.getKind() == StandardWatchEventKinds.ENTRY_CREATE) {
				// DELETE, then CREATE, so merge into a MODIFY
				previous.updateReference(
						FileWatchEvent.fromWithKind(previous.getReference(), StandardWatchEventKinds.ENTRY_MODIFY));
				return true;
			}
		}

		if (previous.getReference().getKind() == StandardWatchEventKinds.ENTRY_CREATE) {
			if (current.getKind() == StandardWatchEventKinds.ENTRY_MODIFY) {
				// skip modify
				return true;
			}
		}

		if (previous.getReference().getKind() == StandardWatchEventKinds.ENTRY_CREATE) {
			if (current.getKind() == StandardWatchEventKinds.ENTRY_DELETE) {
				// CREATE then DELETE, so nothing to notify
				previous.clearReference();
				return true;
			}
		}

		return false;
	}

	/**
	 * postpones the post of the specified event, when it will be time to post,
	 * the reference might have been cleaned up
	 *
	 * (package-private for test purposes)
	 */
	void schedulePost(final EventReference event) {
		executor.schedule(new Runnable() {
			@Override
			public void run() {
				synchronized (queue) {
					try {
						if (event.isPresent()) {
							eventBus.post(event.getReference());
						}
					} finally {
						dequeue(event.getKey(), event);
					}
				}
			}
		}, coalescePeriod, TimeUnit.MILLISECONDS);
	}

	/**
	 * remove the specified event from the queue
	 *
	 * (package-private for test purposes)
	 */
	void dequeue(FileWatchEventKey key, EventReference event) {
		Deque<EventReference> fileEvents;
		if ((fileEvents = queue.get(key)) != null) {
			if (fileEvents.remove(event) && fileEvents.isEmpty()) {
				queue.remove(key); // no more events for this key, remove the stack
			}
		}
	}

	/**
	 * clear all events
	 *
	 * (package-private for test purposes)
	 */
	void clear() {
		synchronized (queue) {
			queue.clear();
		}
	}

	/**
	 * key used for the storage of an event, composed by file paths, two event with same keys, are for the same physical file
	 */
	static class FileWatchEventKey {
		static FileWatchEventKey fromEvent(FileWatchEvent event) {
			return new FileWatchEventKey(event.getDir(), event.getPath());
		}

		private final Path dir;
		private final Path path;

		private FileWatchEventKey(Path dir, Path path) {
			this.dir = dir;
			this.path = path;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (!(o instanceof FileWatchEventKey))
				return false;

			FileWatchEventKey that = (FileWatchEventKey) o;

			return dir.equals(that.dir) && path.equals(that.path);
		}

		@Override
		public int hashCode() {
			int result = dir.hashCode();
			result = 31 * result + path.hashCode();
			return result;
		}
	}

	/**
	 * this is a reference holder, the reference might have been cleaned up, and be null
	 *
	 * it also stores the key of the event, in order to avoid key recalculation
	 */
	static class EventReference {
		static EventReference of(FileWatchEventKey key, FileWatchEvent reference) {
			return new EventReference(key, reference);
		}

		private final FileWatchEventKey key;
		private FileWatchEvent reference;

		private EventReference(FileWatchEventKey key, FileWatchEvent reference) {
			this.key = key;
			this.reference = reference;
		}

		public void updateReference(FileWatchEvent newEvent) {
			reference = newEvent;
		}

		public void clearReference() {
			reference = null;
		}

		public boolean isPresent() {
			return reference != null;
		}

		public FileWatchEventKey getKey() {
			return key;
		}

		public FileWatchEvent getReference() {
			return reference;
		}
	}
}
