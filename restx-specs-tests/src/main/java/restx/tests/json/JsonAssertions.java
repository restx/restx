package restx.tests.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.Files;
import restx.common.MoreFiles;
import restx.common.Mustaches;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    private int contentLengthHtmlReportThreshold = 600;

    private JsonAssertions(JsonSource actual) {
        this.actual = actual;
        differ = new JsonDiffer();
    }

    public JsonAssertions allowingExtraUnexpectedFields() {
        allowingExtraUnexpectedFields = true;
        differ.getLeftConfig().setIgnoreExtraFields(true);
        return this;
    }

    public JsonAssertions withJsonDiffComparator(JsonDiffComparator jsonDiffComparator) {
        differ.setJsonDiffComparator(jsonDiffComparator);
        return this;
    }

    public JsonAssertions withContentLengthHtmlReportThreshold(final int contentLengthHtmlReportThreshold) {
        this.contentLengthHtmlReportThreshold = contentLengthHtmlReportThreshold;
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
            if (expected.content().length() < contentLengthHtmlReportThreshold
                    && actual.content().length() < contentLengthHtmlReportThreshold) {
                throw new AssertionError(fullTextReport(expected, diff).toString());
            } else {
                StringBuilder sb = new StringBuilder();

                sb.append("Expecting:\n")
                        .append(indent(limit(actual.content(), contentLengthHtmlReportThreshold), 2)).append("\n")
                        .append("\nto be same json as:\n")
                        .append(indent(limit(expected.content(), contentLengthHtmlReportThreshold), 2)).append("\n")
                        .append("\nbut following differences were found:\n\n");

                int i = 1;
                for (JsonDiff.Difference difference : diff.getDifferences()) {
                    appendDifferenceInfo(i, sb, difference);
                    i++;
                }

                try {
                    File htmlReport = File.createTempFile("json-diff", ".html");

                    List<Object> differences = new ArrayList<>();
                    ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();
                    i = 1;
                    for (JsonDiff.Difference difference : diff.getDifferences()) {
                        StringBuilder diffSb = new StringBuilder();
                        appendDifferenceInfo(i, diffSb, difference);

                        String actualKeyOfInterest;
                        String expectedKeyOfInterest;
                        String actualContextPath;
                        String expectedContextPath;

                        // for added and removed keys, the interesting context is at captured path.
                        // for others it's more interesting to get up one level
                        if (difference instanceof JsonDiff.AddedKey) {
                            actualKeyOfInterest = "";
                            actualContextPath = difference.getLeftPath();
                            expectedKeyOfInterest = ((JsonDiff.AddedKey) difference).getKey();
                            expectedContextPath = difference.getRightPath();
                        } else if (difference instanceof JsonDiff.RemovedKey) {
                            actualKeyOfInterest = ((JsonDiff.RemovedKey) difference).getKey();
                            actualContextPath = difference.getLeftPath();
                            expectedKeyOfInterest = "";
                            expectedContextPath = difference.getRightPath();
                        } else {
                            actualKeyOfInterest = diff.getLastElementPath(difference.getLeftPath());
                            expectedKeyOfInterest = diff.getLastElementPath(difference.getRightPath());
                            actualContextPath = diff.getParentPath(difference.getLeftPath());
                            expectedContextPath = diff.getParentPath(difference.getRightPath());
                        }

                        differences.add(
                                ImmutableMap.builder()
                                        .put("number", String.valueOf(i))
                                        .put("difference", diffSb.toString().replace("\"", "\\\"").replace("\n", "\\n"))
                                        .put("actual-path", difference.getLeftPath())
                                        .put("actual-keyOfInterest", actualKeyOfInterest)
                                        .put("actual-context", objectWriter.writeValueAsString(
                                            toContext(diff.getLeftAt(actualContextPath),
                                                        actualKeyOfInterest))
                                                    .replace("\"", "\\\"").replace("\n", "\\n"))
                                        .put("expected-path", difference.getRightPath())
                                        .put("expected-keyOfInterest", expectedKeyOfInterest)
                                        .put("expected-context", objectWriter.writeValueAsString(
                                            toContext(diff.getRightAt(expectedContextPath),
                                                    expectedKeyOfInterest))
                                                    .replace("\"", "\\\"").replace("\n", "\\n"))
                                .build()
                                );
                         i++;
                    }

                    String r = Mustaches.execute(Mustaches.compile(JsonAssertions.class, "json-diff.html"),
                            ImmutableMap.of(
                                    "actual", actual.content(),
                                    "expected", expected.content(),
                                    "diff", diff,
                                    "differences", differences,
                                    "fix-expected", new JsonMerger().mergeToRight(diff)
                            ));

                    Files.write(r, htmlReport, Charsets.UTF_8);

                    sb.append("\n\nA detailed HTML REPORT has been generated in ")
                            .append(htmlReport.toURI().toURL()).append("\n");
                } catch (IOException e) {
                    sb.append("\n\nERROR occured when generating html report: " + e + "\n\n");
                }

                throw new AssertionError(sb.toString());
            }
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    private Object toContext(Object o, String keyOfInterest) {
        if (o instanceof Map) {
            Map<String, Object> map = (Map) o;
            Map<String, Object> context = new LinkedHashMap<>();

            for (String key : map.keySet()) {
                Object v = map.get(key);
                if (key.equals(keyOfInterest)) {
                    context.put(key, v);
                } else {
                    if (v instanceof Map) {
                        context.put(key, "/object with " + ((Map) v).size() + " entries/");
                    } else if (v instanceof List) {
                        context.put(key, "/array with " + ((List) v).size() + " entries/");
                    } else {
                        context.put(key, v);
                    }
                }
            }
            return context;
        }

        return o;
    }

    private String limit(String s, int threshold) {
        if (s.length() <= threshold) {
            return s;
        }
        return s.substring(0, threshold) + "[...]\n[" + (s.length() - threshold) + " chars stripped]";
    }

    protected StringBuilder fullTextReport(JsonSource expected, JsonDiff diff) {
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
                appendDifferenceInfo(i, sb, difference);
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
        return sb;
    }

    private void appendDifferenceInfo(int i, StringBuilder sb, JsonDiff.Difference difference) {
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
    }

    private String asJson(Object o) {
        if (o == null) {
            return "null";
        }

        try {
            return MoreFiles.removeWindowsCarriageReturnsBeforeLF(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(o));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return o.toString();
        }
    }
}
