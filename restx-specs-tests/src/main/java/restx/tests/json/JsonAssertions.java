package restx.tests.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;

import static restx.common.MoreStrings.indent;

/**
 * Date: 4/2/14
 * Time: 21:34
 */
public class JsonAssertions {

    public static JsonAssertions assertThat(String json) {
        return new JsonAssertions(new StringJsonSource("actual", json));
    }
    public static JsonAssertions assertThat(File json, Charset cs) {
        return new JsonAssertions(new FileJsonSource(json, cs));
    }
    public static JsonAssertions assertThat(URL json, Charset cs) {
        return new JsonAssertions(new URLJsonSource(json, cs));
    }
    public static JsonAssertions assertThat(JsonSource json) {
        return new JsonAssertions(json);
    }

    private final JsonDiffer differ;
    private final JsonSource actual;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private boolean allowingExtraUnexpectedFields;

    private JsonAssertions(JsonSource actual) {
        this.actual = actual;
        differ = new JsonDiffer();
    }

    public JsonAssertions allowingExtraUnexpectedFields() {
        allowingExtraUnexpectedFields = true;
        differ.getLeftConfig().setIgnoreExtraFields(true);
        return this;
    }

    public JsonAssertions isSameJsonAs(String expected) {
        return isSameJsonAs(new StringJsonSource("expected", expected));
    }
    public JsonAssertions isSameJsonAs(File expected, Charset cs) {
        return isSameJsonAs(new FileJsonSource(expected, cs));
    }
    public JsonAssertions isSameJsonAs(URL expected, Charset cs) {
        return isSameJsonAs(new URLJsonSource(expected, cs));
    }
    public JsonAssertions isSameJsonAs(JsonSource expected) {
        JsonDiff diff = differ.compare(actual, expected);

        if (!diff.isSame()) {
            StringBuilder sb = new StringBuilder();

            sb.append("Expecting:\n")
                    .append(indent(actual.content(), 2)).append("\n")
                    .append("to be same json as:\n")
                    .append(indent(expected.content(), 2)).append("\n")
                    .append("but following differences were found:\n");

            Multimap<JsonObjectLocation, JsonDiff.Difference> differencesPerLocation = LinkedListMultimap.create();
            for (JsonDiff.Difference difference : diff.getDifferences()) {
                differencesPerLocation.put(difference.getLeftContext(), difference);
            }

            int i = 1;
            for (JsonObjectLocation context : differencesPerLocation.keySet()) {
                sb.append(String.format("- within [L%dC%d]-[L%dC%d]:\n",
                        context.getFrom().getLineNr(), context.getFrom().getColumnNr(),
                        context.getTo().getLineNr(), context.getTo().getColumnNr()))
                        .append(indent(context.getJson(), 2))
                        .append("\n");
                for (JsonDiff.Difference difference : differencesPerLocation.get(context)) {
                    if (difference instanceof JsonDiff.AddedKey) {
                        JsonDiff.AddedKey addedKey = (JsonDiff.AddedKey) difference;
                        sb.append(String.format("%02d) ", i))
                                .append("missing key '").append(addedKey.getKey()).append("'")
                                .append(" at path '").append(addedKey.getLeftPath()).append("'")
                                .append(" expected value:\n")
                                .append(indent(asJson(addedKey.getValue()), 6))
                                .append("\n");;
                    }
                    if (difference instanceof JsonDiff.RemovedKey) {
                        JsonDiff.RemovedKey removedKey = (JsonDiff.RemovedKey) difference;

                        sb.append(String.format("%02d) ", i))
                                .append("extra key '").append(removedKey.getKey()).append("'")
                                .append(" at path '").append(removedKey.getLeftPath()).append("'")
                                .append(" with value:\n")
                                .append(indent(asJson(removedKey.getValue()), 6))
                                .append("\n");;
                    }
                    if (difference instanceof JsonDiff.ValueDiff) {
                        JsonDiff.ValueDiff valueDiff = (JsonDiff.ValueDiff) difference;

                        sb.append(String.format("%02d) ", i))
                                .append("expected value ").append(asJson(valueDiff.getRightValue()))
                                .append(" but was ").append(asJson(valueDiff.getLeftValue()))
                                .append(" at path '").append(valueDiff.getLeftPath()).append("'\n");
                    }
                    if (difference instanceof JsonDiff.ArrayInsertedValue) {
                        JsonDiff.ArrayInsertedValue arrayInsertedValue = (JsonDiff.ArrayInsertedValue) difference;

                        sb.append(String.format("%02d) ", i))
                                .append("missing element(s) in array at position ").append(arrayInsertedValue.getLeftPosition())
                                .append(" at path '").append(arrayInsertedValue.getLeftPath()).append("'")
                                .append(" expected value(s):\n")
                                .append(indent(asJson(arrayInsertedValue.getValues()), 6))
                                .append("\n");
                    }
                    if (difference instanceof JsonDiff.ArrayDeletedValue) {
                        JsonDiff.ArrayDeletedValue arrayDeletedValue = (JsonDiff.ArrayDeletedValue) difference;

                        sb.append(String.format("%02d) ", i))
                                .append("extra element(s) in array at position ").append(arrayDeletedValue.getLeftPosition())
                                .append(" at path '").append(arrayDeletedValue.getLeftPath()).append("'")
                                .append(" with value(s):\n")
                                .append(indent(asJson(arrayDeletedValue.getValues()), 6))
                                .append("\n");
                    }
                    i++;
                }
            }

            // merging can be done with simple copy paste when not allowing extra fields,
            // but with extra fields allowed copying the actual content when it's the expectation which is not
            // up to date leads to adding previously ignored fields to the expectation.
            // therefore we dump a merged expect in this case to ease test maintenance.
            if (allowingExtraUnexpectedFields) {
                sb.append("\n\nif the expectation is not up to date, here is a merged" +
                        " expect that you can use to fix your test:\n");
                sb.append(indent(new JsonMerger().mergeToRight(diff), 2)).append("\n\n");
            }

            throw new AssertionError(sb.toString());
        }
        return this;
    }

    private String asJson(Object o) {
        if (o == null) {
            return "null";
        }

        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(o);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return o.toString();
        }
    }
}
