package restx.tests.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Ordering;
import restx.common.MoreFiles;

import java.util.Comparator;

/**
 * Date: 5/2/14
 * Time: 12:07
 */
public class JsonMerger {
    /*
    We have a lot of symmetry here, it would much cleaner to be able to address left and right with a parameter to avoid
    the copy pasting. But the code is colocated so maintenance is easy, so we'll stick with it ATM
     */

    private final ObjectMapper mapper = new ObjectMapper();
    private final Comparator<JsonDiff.Difference> rightPositionInversedSorter = new Comparator<JsonDiff.Difference>() {
        @Override
        public int compare(JsonDiff.Difference o1, JsonDiff.Difference o2) {
            if (o1 instanceof JsonDiff.ArrayDiff && o2 instanceof JsonDiff.ArrayDiff) {
                JsonDiff.ArrayDiff arrayDiff1 = (JsonDiff.ArrayDiff) o1;
                JsonDiff.ArrayDiff arrayDiff2 = (JsonDiff.ArrayDiff) o2;

                if (arrayDiff1.getValues() == arrayDiff2.getValues()) {
                    return Integer.compare(arrayDiff2.getRightPosition(), arrayDiff1.getRightPosition());
                }
            }
            // for other differences we don't really care, all we need is to return a consistent value
            return Integer.compare(o1.hashCode(), o2.hashCode());
        }
    };
    private final Comparator<JsonDiff.Difference> leftPositionInversedSorter = new Comparator<JsonDiff.Difference>() {
        @Override
        public int compare(JsonDiff.Difference o1, JsonDiff.Difference o2) {
            if (o1 instanceof JsonDiff.ArrayDiff && o2 instanceof JsonDiff.ArrayDiff) {
                JsonDiff.ArrayDiff arrayDiff1 = (JsonDiff.ArrayDiff) o1;
                JsonDiff.ArrayDiff arrayDiff2 = (JsonDiff.ArrayDiff) o2;

                if (arrayDiff1.getValues() == arrayDiff2.getValues()) {
                    return Integer.compare(arrayDiff2.getLeftPosition(), arrayDiff1.getLeftPosition());
                }
            }
            // for other differences we don't really care, all we need is to return a consistent value
            return Integer.compare(o1.hashCode(), o2.hashCode());
        }
    };

    public String mergeToRight(JsonDiff diff) {
        Object root = diff.getRightObj().getRoot();

        for (JsonDiff.Difference difference : Ordering.from(rightPositionInversedSorter).sortedCopy(diff.getDifferences())) {
            difference.mergeToRight(diff);
        }

        return serializeNodeAsString(root);
    }

    public String mergeToLeft(JsonDiff diff) {
        Object root = diff.getRightObj().getRoot();

        for (JsonDiff.Difference difference : Ordering.from(leftPositionInversedSorter).sortedCopy(diff.getDifferences())) {
            difference.mergeToLeft(diff);
        }

        return serializeNodeAsString(root);
    }

    private String serializeNodeAsString(Object root) {
        try {
            return MoreFiles.removeWindowsCarriageReturnsBeforeLF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
