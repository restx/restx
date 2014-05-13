package restx.security;

import org.joda.time.Duration;
import org.junit.Test;
import restx.factory.Factory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Date: 13/5/14
 * Time: 21:01
 */
public class RestxSessionTest {
    @Test
    public void should_run_in_isolate_session() throws Exception {
        final EmptySessionProvider sessionProvider = Factory.getInstance().getComponent(EmptySessionProvider.class);
        final RestxSession restxSession = sessionProvider.get();
        final int[] executed = new int[]{0};

        restxSession.runIn(new Runnable() {
            @Override
            public void run() {
                // current session should be the empty session initialized
                assertThat(RestxSession.current()).isSameAs(restxSession);
                assertThat(RestxSession.current().getExpires()).isEqualTo(Duration.ZERO);

                sessionProvider.get().runIn(new Runnable() {
                    @Override
                    public void run() {
                        RestxSession.current().expires(Duration.millis(100));

                        assertThat(RestxSession.current().getExpires()).isEqualTo(Duration.millis(100));
                        executed[0]++;
                    }
                });

                // current session should not be impacted by the call of other runnable
                assertThat(RestxSession.current().getExpires()).isEqualTo(Duration.ZERO);
                executed[0]++;
            }
        });

        assertThat(executed[0]).isEqualTo(2);
    }
}
