package restx.json.testing.twitter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.common.io.Resources;
import restx.json.testing.JsonBench;

import java.io.*;

/**
 * Benchmark inspired by: https://github.com/FasterXML/jvm-json-benchmark
 * Comparing only jackson with rxjson, jackson being the best so far.
 */
public class TwitterBench {
    private static class JacksonBench extends JsonBench<TwitterSearch> {
        private static ObjectMapper objectMapper = new ObjectMapper();

        ObjectReader jacksonReader = objectMapper.reader(TwitterSearch.class);

        public JacksonBench(int threads, int count, long reps, byte[] data) {
            super("jackson", threads, count, reps, data);
        }

        @Override
        protected TwitterSearch parse(InputStream stream) throws IOException {
            return jacksonReader.readValue(stream);
        }
    }

    private static class RxJsonBench extends JsonBench<TwitterSearch> {
        public RxJsonBench(int threads, int count, long reps, byte[] data) {
            super("rxjson", threads, count, reps, data);
        }

        @Override
        protected TwitterSearch parse(InputStream stream) throws Exception {
            return TwitterSearchParser.parse(new InputStreamReader(stream, "UTF-8"));
        }
    }

    public static void main(String[] args) throws Exception {
        byte[] data = Resources.toByteArray(Resources.getResource("twitter-search.json"));


        System.out.println("Twitter Search JSON binding Micro Benchmark\n" +
                "- each json is a twitter search result object \n" +
                "   - with 15 twitter entries\n" +
                "   - made of " + data.length + " bytes\n" +
                "   - parsed from a ByteArrayInputStream (bytes loaded only once at start of bench)");
        new JacksonBench(Runtime.getRuntime().availableProcessors(), 10, 10000, data).bench();
        new RxJsonBench(Runtime.getRuntime().availableProcessors(), 10, 10000, data).bench();
    }
}
