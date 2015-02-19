package restx.common.watch;

import static org.assertj.core.api.Assertions.assertThat;


import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * User: xavierhanin
 * Date: 9/11/13
 * Time: 5:53 PM
 */
public class EventCoalescorTest {
    @Test
    public void should_coalesce() throws Exception {
        EventBus eventBus = new EventBus();
        final List<String> messages = new ArrayList<>();

        eventBus.register(new Object() {
            @Subscribe
            public void onMessage(String msg) {
                messages.add(msg);
            }
        });

        EventCoalescor coalescor = EventCoalescor.generic(eventBus, 30);

        coalescor.post("test1");
        coalescor.post("test2");
        coalescor.post("test1");
        coalescor.post("test3");

        Thread.sleep(40);

        assertThat(messages).containsExactly("test1", "test2", "test3");

        messages.clear();
        coalescor.post("test2");
        coalescor.post("test1");
        coalescor.post("test1");
        coalescor.post("test1");
        coalescor.post("test1");
        Thread.sleep(40);

        assertThat(messages).containsExactly("test2", "test1");
    }
}
