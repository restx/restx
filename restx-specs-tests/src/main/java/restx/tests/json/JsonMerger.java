package restx.tests.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Date: 5/2/14
 * Time: 12:07
 */
public class JsonMerger {
    private final ObjectMapper mapper = new ObjectMapper();

    public String mergeToRight(JsonDiff diff) {
        Object root = diff.getRightObj().getRoot();

        for (JsonDiff.Difference difference : diff.getDifferences()) {
            difference.mergeToRight(diff);
        }

        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public String mergeToLeft(JsonDiff diff) {
        Object root = diff.getRightObj().getRoot();

        for (JsonDiff.Difference difference : diff.getDifferences()) {
            difference.mergeToLeft(diff);
        }

        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
