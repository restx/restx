package restx.common.watch;

import static org.assertj.core.api.Assertions.assertThat;


import com.google.common.eventbus.EventBus;
import org.junit.Test;

import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FileWatchEventCoalescorTest {

	/**
	 * A utility object for the test, that wrap a {@link FileWatchEventCoalescor}
	 * and keep reference on events that are scheduled to be posted. It also permits
	 * to clean the event queue manually.
	 */
	static class TestFileWatchEventCoalescor {
		final List<FileWatchEventCoalescor.EventReference> scheduledEvents = new ArrayList<>();

		FileWatchEventCoalescor coalescor = new FileWatchEventCoalescor(new EventBus(), 50) { // event bus and coalesce time are ignored
			@Override
			void schedulePost(EventReference event) {
				scheduledEvents.add(event);
			}
		};

		FileWatchEvent post(String filePath, WatchEvent.Kind<?> kind) {
			FileWatchEvent event = FileWatchEvent.newInstance(Paths.get("/"), Paths.get("/"), Paths.get(filePath), kind, 1);
			coalescor.post(event);
			return event;
		}

		/*
			remove noisy event, and return true if it manage to find it
		 */
		boolean removeNoise(FileWatchEvent noise) {
			for (Iterator<FileWatchEventCoalescor.EventReference> it = scheduledEvents.iterator(); it.hasNext(); ) {
				FileWatchEventCoalescor.EventReference ref = it.next();
				if (ref.isPresent() && ref.getReference() == noise) {
					coalescor.dequeue(ref.getKey(), ref);
					it.remove();
					return true;
				}
			}
			return false;
		}

		void clear() {
			coalescor.clear();
		}
	}

	/*
		No merges here, just very basic events, on separate files
	 */
	@Test
	public void should_send_events() {
		TestFileWatchEventCoalescor watchEventCoalescor = new TestFileWatchEventCoalescor();
		watchEventCoalescor.post("tmp/foo", StandardWatchEventKinds.ENTRY_CREATE);
		watchEventCoalescor.post("tmp/bar", StandardWatchEventKinds.ENTRY_DELETE);
		watchEventCoalescor.post("tmp/test", StandardWatchEventKinds.ENTRY_MODIFY);
		watchEventCoalescor.post("tmp/another_file.txt", StandardWatchEventKinds.ENTRY_CREATE);

		assertThat(watchEventCoalescor.scheduledEvents).extracting("reference").extracting("path")
				.containsOnly(
						Paths.get("tmp/foo"),
						Paths.get("tmp/bar"),
						Paths.get("tmp/test"),
						Paths.get("tmp/another_file.txt")
				);
	}

	@Test
	public void should_merge_duplicate_events() {
		TestFileWatchEventCoalescor watchEventCoalescor = new TestFileWatchEventCoalescor();
		FileWatchEvent noise;

		// try with some create events

		watchEventCoalescor.post("tmp/foo", StandardWatchEventKinds.ENTRY_CREATE);
		watchEventCoalescor.post("tmp/foo", StandardWatchEventKinds.ENTRY_CREATE);
		noise = watchEventCoalescor.post("tmp/bar", StandardWatchEventKinds.ENTRY_CREATE); // just to add some noise
		watchEventCoalescor.post("tmp/foo", StandardWatchEventKinds.ENTRY_CREATE);

		assertThat(watchEventCoalescor.removeNoise(noise)).isTrue();

		assertThat(watchEventCoalescor.scheduledEvents).hasSize(1);
		FileWatchEvent event = watchEventCoalescor.scheduledEvents.get(0).getReference();
		assertThat(event.getPath()).isEqualTo(Paths.get("tmp/foo"));
		assertThat(event.getKind()).isEqualTo(StandardWatchEventKinds.ENTRY_CREATE);

		watchEventCoalescor.clear();

		// try with some delete events

		watchEventCoalescor.post("tmp/foo", StandardWatchEventKinds.ENTRY_DELETE);
		watchEventCoalescor.post("tmp/foo", StandardWatchEventKinds.ENTRY_DELETE);
		noise = watchEventCoalescor.post("tmp/bar", StandardWatchEventKinds.ENTRY_CREATE); // just to add some noise
		watchEventCoalescor.post("tmp/foo", StandardWatchEventKinds.ENTRY_DELETE);

		assertThat(watchEventCoalescor.removeNoise(noise)).isTrue(); // remove noisy event

		assertThat(watchEventCoalescor.scheduledEvents).hasSize(2);
		event = watchEventCoalescor.scheduledEvents.get(1).getReference();
		assertThat(event.getPath()).isEqualTo(Paths.get("tmp/foo"));
		assertThat(event.getKind()).isEqualTo(StandardWatchEventKinds.ENTRY_DELETE);

		watchEventCoalescor.clear();

		// try with some delete modify

		watchEventCoalescor.post("tmp/foo", StandardWatchEventKinds.ENTRY_MODIFY);
		noise = watchEventCoalescor.post("tmp/bar", StandardWatchEventKinds.ENTRY_CREATE); // just to add some noise
		watchEventCoalescor.post("tmp/foo", StandardWatchEventKinds.ENTRY_MODIFY);
		watchEventCoalescor.post("tmp/foo", StandardWatchEventKinds.ENTRY_MODIFY);

		assertThat(watchEventCoalescor.removeNoise(noise)).isTrue(); // remove noisy event

		assertThat(watchEventCoalescor.scheduledEvents).hasSize(3);
		event = watchEventCoalescor.scheduledEvents.get(2).getReference();
		assertThat(event.getPath()).isEqualTo(Paths.get("tmp/foo"));
		assertThat(event.getKind()).isEqualTo(StandardWatchEventKinds.ENTRY_MODIFY);

		watchEventCoalescor.clear();
	}
	@Test
	public void should_merge_delete_with_create_events() {

		TestFileWatchEventCoalescor watchEventCoalescor = new TestFileWatchEventCoalescor();
		watchEventCoalescor.post("tmp/foo", StandardWatchEventKinds.ENTRY_DELETE);
		FileWatchEvent noise = watchEventCoalescor.post("tmp/bar", StandardWatchEventKinds.ENTRY_CREATE);// just to add some noise
		watchEventCoalescor.post("tmp/foo", StandardWatchEventKinds.ENTRY_CREATE);

		assertThat(watchEventCoalescor.removeNoise(noise)).isTrue(); // remove noisy event

		assertThat(watchEventCoalescor.scheduledEvents).hasSize(1);
		FileWatchEvent event = watchEventCoalescor.scheduledEvents.get(0).getReference();
		assertThat(event.getPath()).isEqualTo(Paths.get("tmp/foo"));
		assertThat(event.getKind()).isEqualTo(StandardWatchEventKinds.ENTRY_MODIFY);
	}

	@Test
	public void should_merge_create_with_modify_events() {
		TestFileWatchEventCoalescor watchEventCoalescor = new TestFileWatchEventCoalescor();
		watchEventCoalescor.post("tmp/foo", StandardWatchEventKinds.ENTRY_CREATE);
		FileWatchEvent noise = watchEventCoalescor.post("tmp/bar", StandardWatchEventKinds.ENTRY_CREATE);// just to add some noise
		watchEventCoalescor.post("tmp/foo", StandardWatchEventKinds.ENTRY_MODIFY);

		assertThat(watchEventCoalescor.removeNoise(noise)).isTrue(); // remove noisy event

		assertThat(watchEventCoalescor.scheduledEvents).hasSize(1);
		FileWatchEvent event = watchEventCoalescor.scheduledEvents.get(0).getReference();
		assertThat(event.getPath()).isEqualTo(Paths.get("tmp/foo"));
		assertThat(event.getKind()).isEqualTo(StandardWatchEventKinds.ENTRY_CREATE);
	}

	@Test
	public void should_remove_consecutive_create_and_delete_events() {
		TestFileWatchEventCoalescor watchEventCoalescor = new TestFileWatchEventCoalescor();
		watchEventCoalescor.post("tmp/foo", StandardWatchEventKinds.ENTRY_CREATE);
		FileWatchEvent noise = watchEventCoalescor.post("tmp/bar", StandardWatchEventKinds.ENTRY_CREATE);// just to add some noise
		watchEventCoalescor.post("tmp/foo", StandardWatchEventKinds.ENTRY_DELETE);

		assertThat(watchEventCoalescor.removeNoise(noise)).isTrue(); // remove noisy event

		assertThat(watchEventCoalescor.scheduledEvents).hasSize(1);
		assertThat(watchEventCoalescor.scheduledEvents.get(0).isPresent()).isFalse();
	}

	@Test
	public void should_only_merge_consecutive_events_for_a_file() {
		TestFileWatchEventCoalescor watchEventCoalescor = new TestFileWatchEventCoalescor();
		watchEventCoalescor.post("tmp/foo", StandardWatchEventKinds.ENTRY_MODIFY);
		FileWatchEvent noise1 = watchEventCoalescor.post("tmp/bar", StandardWatchEventKinds.ENTRY_CREATE);// just to add some noise
		watchEventCoalescor.post("tmp/foo", StandardWatchEventKinds.ENTRY_DELETE);
		watchEventCoalescor.post("tmp/foo", StandardWatchEventKinds.ENTRY_MODIFY);
		watchEventCoalescor.post("tmp/bar", StandardWatchEventKinds.ENTRY_CREATE);// just to add some noise
		watchEventCoalescor.post("tmp/foo", StandardWatchEventKinds.ENTRY_CREATE);
		watchEventCoalescor.post("tmp/foo", StandardWatchEventKinds.ENTRY_CREATE);

		assertThat(watchEventCoalescor.removeNoise(noise1)).isTrue(); // remove noisy event (only one, they have been merged)

		assertThat(watchEventCoalescor.scheduledEvents).hasSize(4); // MODIFY, DELETE, MODIFY, CREATE
		assertThat(watchEventCoalescor.scheduledEvents).extracting("reference").extracting("kind")
				.containsExactly(
						StandardWatchEventKinds.ENTRY_MODIFY,
						StandardWatchEventKinds.ENTRY_DELETE,
						StandardWatchEventKinds.ENTRY_MODIFY,
						StandardWatchEventKinds.ENTRY_CREATE
				);
	}

	@Test
	public void should_merge_classic_idea_on_windows_behavior() {
		TestFileWatchEventCoalescor watchEventCoalescor = new TestFileWatchEventCoalescor();
		watchEventCoalescor.post("tmp/foo", StandardWatchEventKinds.ENTRY_DELETE);
		FileWatchEvent noise1 = watchEventCoalescor.post("tmp/bar", StandardWatchEventKinds.ENTRY_CREATE);// just to add some noise
		watchEventCoalescor.post("tmp/foo", StandardWatchEventKinds.ENTRY_CREATE);
		watchEventCoalescor.post("tmp/bar", StandardWatchEventKinds.ENTRY_CREATE);// just to add some noise
		watchEventCoalescor.post("tmp/foo", StandardWatchEventKinds.ENTRY_MODIFY);

		assertThat(watchEventCoalescor.removeNoise(noise1)).isTrue(); // remove noisy event

		assertThat(watchEventCoalescor.scheduledEvents).hasSize(1);
		FileWatchEvent event = watchEventCoalescor.scheduledEvents.get(0).getReference();
		assertThat(event.getPath()).isEqualTo(Paths.get("tmp/foo"));
		assertThat(event.getKind()).isEqualTo(StandardWatchEventKinds.ENTRY_MODIFY);
	}
}