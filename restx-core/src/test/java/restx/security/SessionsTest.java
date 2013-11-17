package restx.security;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

/**
 * Date: 17/11/13
 * Time: 17:03
 */
public class SessionsTest {
    @Test
    public void should_touch_data() throws Exception {
        Sessions sessions = new Sessions(10);

        Sessions.SessionData data = sessions.touch("k1", ImmutableMap.<String, String>of());

        assertThat(data).isNotNull();
        assertThat(data.getKey()).isNotNull().isEqualTo("k1");

        Optional<Sessions.SessionData> k1 = sessions.get("k1");
        assertThat(k1).isEqualTo(Optional.of(data));

        Sessions.SessionData touched = sessions.touch("k1", ImmutableMap.<String, String>of());
        assertThat(touched != data).isTrue();

        assertThat(sessions.getAll()).containsOnly(entry("k1", touched));
    }

    @Test
    public void should_limit_size() throws Exception {
        Sessions sessions = new Sessions(2);
        sessions.touch("k1", ImmutableMap.<String, String>of());
        Thread.sleep(1);
        sessions.touch("k2", ImmutableMap.<String, String>of());
        assertThat(sessions.getAll()).containsKeys("k1", "k2").hasSize(2);
        Thread.sleep(1);
        sessions.touch("k3", ImmutableMap.<String, String>of());
        assertThat(sessions.getAll()).containsKeys("k2", "k3").hasSize(2);
        Thread.sleep(1);
        sessions.touch("k2", ImmutableMap.<String, String>of());
        assertThat(sessions.getAll()).containsKeys("k2", "k3").hasSize(2);
        Thread.sleep(1);
        sessions.touch("k4", ImmutableMap.<String, String>of());
        assertThat(sessions.getAll()).containsKeys("k2", "k4").hasSize(2);
    }
}
