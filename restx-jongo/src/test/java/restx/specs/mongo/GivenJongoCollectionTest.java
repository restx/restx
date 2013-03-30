package restx.specs.mongo;

import com.google.common.io.Resources;
import org.fest.assertions.data.MapEntry;
import org.junit.Test;
import restx.specs.RestxSpec;
import restx.specs.RestxSpecLoader;

import java.nio.charset.Charset;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.extractProperty;

/**
 * User: xavierhanin
 * Date: 3/12/13
 * Time: 9:52 PM
 */
public class GivenJongoCollectionTest {

    private RestxSpecLoader restxSpecLoader = new RestxSpecLoader();

    @Test
    public void should_load_from_yaml() throws Exception {
        RestxSpec testCase = restxSpecLoader.load(
                Resources.newReaderSupplier(
                        Resources.getResource("restx/tests/restx_test_case_example_1.yaml"),
                        Charset.forName("UTF-8")));

        assertThat(testCase).isNotNull();
        assertThat(testCase.getTitle()).isEqualTo("should validate salary for casual reception 1st month");
        assertThat(extractProperty("collection").from(testCase.getGiven()))
                .containsExactly("contracts", "events", "salaries");
        assertThat(extractProperty("data").from(testCase.getGiven()))
                .containsExactly(
                        "[ { \"_id\": \"511bd1267638b9481a66f385\", \"title\": \"test1\" } ]\n",
                        "[\n" +
                                "{ \"_id\": \"511bd1267638b9481a66f385\", \"title\": \"example1\" },\n" +
                                "{ \"_id\": \"511bd1297638b9481a66f386\", \"title\": \"example2\" }\n" +
                                "]\n",
                        "");

        assertThat(extractProperty("method").from(testCase.getWhens()))
                .containsExactly("PUT", "GET", "GET");
        assertThat(extractProperty("path").from(testCase.getWhens()))
                .containsExactly(
                        "contracts/511bc1e97638b9481a66f383/salaries/201212/status",
                        "contracts/511bc1e97638b9481a66f383/salaries/201212",
                        "contracts/511bc1e97638b9481a66f383/calendar/events?start=1355176800000&end=1355263200000");
        assertThat(extractProperty("body").from(testCase.getWhens()))
                .containsExactly("{ \"status\": \"validated\" }", "", "");
        assertThat(extractProperty("then.expectedCode").from(testCase.getWhens()))
                .containsExactly(201, 200, 200);
        assertThat(((RestxSpec.WhenHttpRequest) testCase.getWhens().get(0)).getCookies()).contains(
                MapEntry.entry("cookie1", "value1"));
        assertThat(((RestxSpec.WhenHttpRequest) testCase.getWhens().get(2)).getCookies()).contains(
                MapEntry.entry("cookie1", "value1"), MapEntry.entry("cookie2", "value2"));
        assertThat(extractProperty("then.expected").from(testCase.getWhens()))
                        .containsExactly(
                                "{ \"status\": \"validated\" }",
                                "{\n" +
                                        "    \"status\": \"validated\",\n" +
                                        "    \"workTime\": { }\n" +
                                        "}",
                                "[\n" +
                                        "    { \"_id\": \"511bd1267638b9481a66f385\", \"title\": \"example1\" },\n" +
                                        "    { \"_id\": \"511bd1297638b9481a66f386\", \"title\": \"example2\" }\n" +
                                        "]");


    }

    @Test
    public void should_generate_yaml() throws Exception {
        RestxSpec testCase = restxSpecLoader.load(
                Resources.newReaderSupplier(
                        Resources.getResource("restx/tests/restx_test_case_example_1.yaml"),
                        Charset.forName("UTF-8")));

        String actual = testCase.toString();
        System.out.println("actual = " + actual);
        assertThat(actual).isEqualTo(Resources.toString(
                                Resources.getResource("restx/tests/expected_restx_case_example_1.yaml"),
                                Charset.forName("UTF-8")));
    }
}
