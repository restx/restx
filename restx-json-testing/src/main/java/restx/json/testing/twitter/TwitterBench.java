package restx.json.testing.twitter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.common.base.Stopwatch;
import com.google.common.io.Resources;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

/**
 * Date: 11/3/14
 * Time: 21:40
 */
public class TwitterBench {
    private static byte[] _data;
    private static ObjectMapper objectMapper;

    public static void main(String[] args) throws Exception {
        _data = Resources.toByteArray(Resources.getResource("twitter-search.jsn"));

        long reps = 10000L;

        Stopwatch stopwatch;

        objectMapper = new ObjectMapper();

        for (int i = 0 ; i<10 ; i++) {
            stopwatch = Stopwatch.createStarted();
            readJackson(reps);
            System.out.println("jackson: " + stopwatch.stop() + " throughput=" + (reps * 1000 / stopwatch.elapsed(TimeUnit.MILLISECONDS)) + "/s");


            stopwatch = Stopwatch.createStarted();
            readRxJson(reps);
            System.out.println("rxjson : " + stopwatch.stop() + " throughput=" + (reps * 1000 / stopwatch.elapsed(TimeUnit.MILLISECONDS)) + "/s");
        }
    }
    private static long readJackson(long reps) throws IOException {
        ObjectReader jacksonReader = objectMapper.reader(TwitterSearch.class);
        long hash = 1;

        while (--reps >= 0) {
            hash += jacksonReader.readValue(new InputStreamReader(inputStream(), "UTF-8")).hashCode();
        }
        return hash;
    }

    private static long readRxJson(long reps) throws Exception {
        long hash = 1;
        while (--reps >= 0) {
            hash += TwitterSearchParser.parse(new InputStreamReader(inputStream(), "UTF-8")).hashCode();
        }
        return hash;
    }

    protected static InputStream inputStream() {
        return new ByteArrayInputStream(_data);
    }
}
