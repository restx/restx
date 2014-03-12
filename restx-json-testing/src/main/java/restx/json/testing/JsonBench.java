package restx.json.testing;

import com.google.common.base.Stopwatch;
import com.google.common.base.Supplier;

import java.io.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Date: 12/3/14
 * Time: 22:21
 */
public abstract class JsonBench<T> {
    private final String name;
    private final int count;
    private final int threads;
    private final long reps;
    private final byte[] data;
    private final ExecutorService executor;
    private final AtomicLong counter = new AtomicLong();

    protected JsonBench(String name, int threads, int count, long reps, byte[] data) {
        this.name = name;
        this.count = count;
        this.reps = reps;
        this.data = data;
        executor = Executors.newFixedThreadPool(this.threads = threads);
    }

    public void bench() throws Exception {
        // warmup
        System.out.println("[WARMUP]   " + name);
        benchParse(reps, readerSupplier());

        System.out.println("[STARTING] " + name + " " + count + " times " + reps + " in " + threads + " threads");
        final CountDownLatch latch = new CountDownLatch(threads);
        Stopwatch stopwatch = Stopwatch.createStarted();
        for (int i = 0; i < threads; i++) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < count; i++) {
                        try {
                            benchParse(reps, readerSupplier());
                            System.out.print(".");
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        counter.addAndGet(reps);
                    }
                    latch.countDown();
                }
            });
        }
        latch.await(10, TimeUnit.SECONDS);
        System.out.println("\n[TOTAL]  " + name + ": elapsed=" + stopwatch.stop() + "   count=" + counter.get()
                + "   throughput=" + (counter.get() * 1000 / stopwatch.elapsed(TimeUnit.MILLISECONDS)) + "/s "
                + (data.length * counter.get() * 1000 / 1024 / 1024 / stopwatch.elapsed(TimeUnit.MILLISECONDS))+"MB/s\n\n");
    }

    protected Supplier<Reader> readerSupplier() {
        return new Supplier<Reader>() {
            @Override
            public Reader get() {
                try {
                    return new InputStreamReader(inputStream(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    long benchParse(long reps, Supplier<Reader> reader) throws Exception {
        long hash = 1;
        while (--reps >= 0) {
            hash += parse(reader.get()).hashCode();
        }
        return hash;
    }


    protected abstract T parse(Reader reader) throws Exception;


    protected InputStream inputStream() {
        return new ByteArrayInputStream(data);
    }
}
