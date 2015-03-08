package restx.tests.json;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Date: 4/2/14
 * Time: 21:45
 */
public class JsonAssertionsTest {
    @Test
    public void should_not_throw_exception_when_same() throws Exception {
        JsonAssertions.assertThat("{}").isSameJsonAs("{}");
    }

    @Test
    public void should_not_throw_exception_on_extra_field_when_allowed() throws Exception {
        JsonAssertions.assertThat("{\"key1\":\"val1\"}")
                .allowingExtraUnexpectedFields().isSameJsonAs("{}");
    }

    @Test
    public void should_throw_exception_on_extra_field_when_not_allowed() throws Exception {
        try {
            JsonAssertions.assertThat("{\"key1\":\"val1\"}").isSameJsonAs("{}");
            fail("should throw error when not same");
        } catch (AssertionError e) {
            assertThat(e).hasMessage(
                    "Expecting:\n" +
                            "  {\"key1\":\"val1\"}\n" +
                            "to be same json as:\n" +
                            "  {}\n" +
                            "but following differences were found:\n" +
                            "- within [L1C2]-[L1C16]:\n" +
                            "  {\"key1\":\"val1\"}\n" +
                            "01) extra key 'key1' at path '.' with value:\n" +
                            "      \"val1\"\n"
            );
        }
    }

    @Test
    public void should_throw_exception_and_provide_fixed_expect_on_ignored_extra_field_with_error() throws Exception {
        try {
            JsonAssertions
                    .assertThat("{\"key1\": [" +
                            "{\"key2\": \"val21\", \"key3\": \"val31\"}, " +
                            "{\"key2\": \"val22\", \"key3\": \"val32\"}" +
                            "]" +
                            "}")
                    .allowingExtraUnexpectedFields()
                    .isSameJsonAs("{\"key1\": [{\"key2\": \"val22\"}]}");
            fail("should throw error when not same");
        } catch (AssertionError e) {
            assertThat(e).hasMessage(
                    "Expecting:\n" +
                            "  {\"key1\": [{\"key2\": \"val21\", \"key3\": \"val31\"}, {\"key2\": \"val22\", \"key3\": \"val32\"}]}\n" +
                            "to be same json as:\n" +
                            "  {\"key1\": [{\"key2\": \"val22\"}]}\n" +
                            "but following differences were found:\n" +
                            "- within [L1C11]-[L1C82]:\n" +
                            "  [{\"key2\": \"val21\", \"key3\": \"val31\"}, {\"key2\": \"val22\", \"key3\": \"val32\"}]\n" +
                            "01) extra element(s) in array at position 0 at path './key1' with value(s):\n" +
                            "      [ {\n" +
                            "        \"key2\" : \"val21\",\n" +
                            "        \"key3\" : \"val31\"\n" +
                            "      } ]\n" +
                            "\n" +
                            "\n" +
                            "if the expectation is not up to date, here is a merged expect that you can use to fix your test:\n" +
                            "  {\n" +
                            "    \"key1\" : [ {\n" +
                            "      \"key2\" : \"val21\",\n" +
                            "      \"key3\" : \"val31\"\n" +
                            "    }, {\n" +
                            "      \"key2\" : \"val22\"\n" +
                            "    } ]\n" +
                            "  }\n" +
                            "\n"
            );
        }
    }

    @Test
    public void should_throw_exception_when_not_same() throws Exception {
        try {
            JsonAssertions.assertThat("{}").isSameJsonAs("{\"key1\":\"val1\"}");
            fail("should throw error when not same");
        } catch (AssertionError e) {
            assertThat(e).hasMessage(
                    "Expecting:\n" +
                            "  {}\n" +
                            "to be same json as:\n" +
                            "  {\"key1\":\"val1\"}\n" +
                            "but following differences were found:\n" +
                            "- within [L1C2]-[L1C3]:\n" +
                            "  {}\n" +
                            "01) missing key 'key1' at path '.' expected value:\n" +
                            "      \"val1\"\n"
            );
        }
    }

    @Test
    public void should_throw_exception_when_diff_value() throws Exception {
        try {
            JsonAssertions.assertThat("{\"key1\":\"val2\"}").isSameJsonAs("{\"key1\":\"val1\"}");
            fail("should throw error when not same");
        } catch (AssertionError e) {
            assertThat(e).hasMessage(
                    "Expecting:\n" +
                            "  {\"key1\":\"val2\"}\n" +
                            "to be same json as:\n" +
                            "  {\"key1\":\"val1\"}\n" +
                            "but following differences were found:\n" +
                            "- within [L1C10]-[L1C15]:\n" +
                            "  \"val2\"\n" +
                            "01) expected value \"val1\" but was \"val2\" at path './key1'\n"
            );
        }
    }

    @Test
    public void should_throw_exception_when_inserted_array_value() throws Exception {
        try {
            JsonAssertions.assertThat("{\"key1\": [{\"key2\": \"val2\"}]}").isSameJsonAs("{\"key1\": []}");
            fail("should throw error when not same");
        } catch (AssertionError e) {
            assertThat(e).hasMessage(
                    "Expecting:\n" +
                            "  {\"key1\": [{\"key2\": \"val2\"}]}\n" +
                            "to be same json as:\n" +
                            "  {\"key1\": []}\n" +
                            "but following differences were found:\n" +
                            "- within [L1C11]-[L1C28]:\n" +
                            "  [{\"key2\": \"val2\"}]\n" +
                            "01) extra element(s) in array at position 0 at path './key1' with value(s):\n" +
                            "      [ {\n" +
                            "        \"key2\" : \"val2\"\n" +
                            "      } ]\n"
            );
        }
    }

    @Test
    public void should_throw_exception_when_missing_value() throws Exception {
        try {
            JsonAssertions.assertThat("{\"key1\": []}").isSameJsonAs("{\"key1\": [{\"key2\": \"val2\"}]}");
            fail("should throw error when not same");
        } catch (AssertionError e) {
            assertThat(e).hasMessage(
                    "Expecting:\n" +
                            "  {\"key1\": []}\n" +
                            "to be same json as:\n" +
                            "  {\"key1\": [{\"key2\": \"val2\"}]}\n" +
                            "but following differences were found:\n" +
                            "- within [L1C11]-[L1C12]:\n" +
                            "  []\n" +
                            "01) missing element(s) in array at position 0 at path './key1' expected value(s):\n" +
                            "      [ {\n" +
                            "        \"key2\" : \"val2\"\n" +
                            "      } ]\n"
            );
        }
    }

}
