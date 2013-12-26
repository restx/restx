package restx.common;

import org.joda.time.DateTimeUtils;
import org.junit.After;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Date: 26/12/13
 * Time: 18:13
 */
public class ThreadLocalMillisProviderTest {
    @After
    public void teardown() {
        // here we may be tempted to call setCurrentMillisSystem() on DateTimeUtils:
        // DateTimeUtils.setCurrentMillisSystem();
        // but then we would reset the time for the whole JVM (well, current classloader)
        // and therefore influence other parallel tests
        // so we have to trust the class under test at least for its clear method.
        ThreadLocalMillisProvider.clear();
    }

    @Test
    public void should_return_fixed_time_in_same_thread() throws Exception {
        long now = System.currentTimeMillis();
        ThreadLocalMillisProvider.setCurrentMillisFixed(now);

        Thread.sleep(10);

        assertThat(DateTimeUtils.currentTimeMillis()).isEqualTo(now);

        ThreadLocalMillisProvider.clear();

        assertThat(DateTimeUtils.currentTimeMillis()).isNotEqualTo(now);
    }

    @Test
    public void should_return_system_time_in_other_thread() throws Exception {
        final long now = System.currentTimeMillis();
        ThreadLocalMillisProvider.setCurrentMillisFixed(now);

        Thread.sleep(10);
        final long[] fromOtherThread = new long[1];
        collectCurrentTimeInThread(fromOtherThread, 0).join();
        assertThat(fromOtherThread[0]).isNotEqualTo(now).isNotEqualTo(0);
    }

    @Test
    public void should_set_fixed_time_in_2_threads() throws Exception {
        final long now = System.currentTimeMillis();
        final long[] fromOtherThread = new long[2];
        Thread t1 = setAndCollectCurrentTimeInThread(fromOtherThread, 0, now);
        Thread t2 = setAndCollectCurrentTimeInThread(fromOtherThread, 1, now + 10);
        t1.join();
        t2.join();

        assertThat(DateTimeUtils.currentTimeMillis()).isNotEqualTo(now).isNotEqualTo(now + 10).isNotEqualTo(0);
        assertThat(fromOtherThread[0]).isEqualTo(now);
        assertThat(fromOtherThread[1]).isEqualTo(now + 10);
    }

    protected Thread setAndCollectCurrentTimeInThread(final long[] fromOtherThread, final int i, final long now) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ThreadLocalMillisProvider.setCurrentMillisFixed(now);
                    Thread.sleep(10);
                    fromOtherThread[i] = DateTimeUtils.currentTimeMillis();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    ThreadLocalMillisProvider.clear();
                }
            }
        });
        thread.start();
        return thread;
    }

    protected Thread collectCurrentTimeInThread(final long[] fromOtherThread, final int i) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                fromOtherThread[i] = DateTimeUtils.currentTimeMillis();
            }
        });
        thread.start();
        return thread;
    }
}
