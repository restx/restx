package restx.specs;

import com.google.common.io.Resources;
import org.fest.assertions.api.Assertions;
import org.junit.Test;

import java.nio.charset.Charset;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * User: xavierhanin
 * Date: 3/12/13
 * Time: 9:52 PM
 */
public class RestxSpecTest {
    @Test
    public void should_load_from_yaml() throws Exception {
        RestxSpec testCase = RestxSpec.load(
                Resources.newReaderSupplier(
                        Resources.getResource("restx/tests/restx_test_case_example_1.yaml"),
                        Charset.forName("UTF-8")));

        assertThat(testCase).isNotNull();
        assertThat(testCase.getTitle()).isEqualTo("should validate salary for casual reception 1st month");
        Assertions.assertThat(Assertions.extractProperty("collection").from(testCase.getGiven()))
                .containsExactly("contracts", "events", "salaries");
        Assertions.assertThat(Assertions.extractProperty("data").from(testCase.getGiven()))
                .containsExactly(
                        "[ { \"_id\": \"511bd1267638b9481a66f385\", \"title\": \"test1\" } ]\n",
                        "[\n" +
                                "{ \"_id\": \"511bd1267638b9481a66f385\", \"title\": \"example1\" },\n" +
                                "{ \"_id\": \"511bd1297638b9481a66f386\", \"title\": \"example2\" }\n" +
                                "]\n",
                        "");

        Assertions.assertThat(Assertions.extractProperty("method").from(testCase.getWhens()))
                .containsExactly("PUT", "GET", "GET");
        Assertions.assertThat(Assertions.extractProperty("path").from(testCase.getWhens()))
                .containsExactly(
                        "contracts/511bc1e97638b9481a66f383/salaries/201212/status",
                        "contracts/511bc1e97638b9481a66f383/salaries/201212",
                        "contracts/511bc1e97638b9481a66f383/calendar/events?start=1355176800000&end=1355263200000");
        Assertions.assertThat(Assertions.extractProperty("body").from(testCase.getWhens()))
                .containsExactly("{ \"status\": \"validated\" }", "", "");
        Assertions.assertThat(Assertions.extractProperty("then.expectedCode").from(testCase.getWhens()))
                .containsExactly(201, 200, 200);
        Assertions.assertThat(Assertions.extractProperty("then.expected").from(testCase.getWhens()))
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
}
