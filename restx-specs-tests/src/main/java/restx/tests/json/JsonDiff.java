package restx.tests.json;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import java.util.*;

import static java.util.Arrays.asList;
import static restx.common.MorePreconditions.checkPresent;

/**
 * Date: 2/2/14
 * Time: 18:49
 */
public class JsonDiff {
    private final JsonWithLocationsParser.ParsedJsonWithLocations leftObj;
    private final JsonWithLocationsParser.ParsedJsonWithLocations rightObj;

    private List<Difference> differences = new ArrayList<>();
    private List<String> currentLeftPath = new LinkedList<>(asList("."));
    private List<String> currentRightPath = new LinkedList<>(asList("."));
    private Map<String, Object> leftContexts = new LinkedHashMap<>();
    private Map<String, Object> rightContexts = new LinkedHashMap<>();

    public JsonDiff(JsonWithLocationsParser.ParsedJsonWithLocations leftObj,
                    JsonWithLocationsParser.ParsedJsonWithLocations rightObj) {
        this.leftObj = leftObj;
        this.rightObj = rightObj;

        putContexts(leftObj.getRoot(), rightObj.getRoot());
    }

    public JsonWithLocationsParser.ParsedJsonWithLocations getLeftObj() {
        return leftObj;
    }

    public JsonWithLocationsParser.ParsedJsonWithLocations getRightObj() {
        return rightObj;
    }

    public boolean isSame() {
        return differences.isEmpty();
    }

    protected JsonDiff goIn(String path) {
        currentLeftPath.add(path);
        currentRightPath.add(path);
        return this;
    }

    public JsonDiff goIn(String left, String right) {
        currentLeftPath.add(left);
        currentRightPath.add(right);
        return this;
    }

    protected JsonDiff goUp() {
        currentLeftPath.remove(currentLeftPath.size() - 1);
        currentRightPath.remove(currentRightPath.size() - 1);
        return this;
    }

    protected String currentLeftPath() {
        return pathFor(currentLeftPath);
    }

    protected String currentRightPath() {
        return pathFor(currentRightPath);
    }

    public JsonDiff addDifference(Difference difference) {
        differences.add(difference);
        return this;
    }

    public ImmutableList<Difference> getDifferences() {
        return ImmutableList.copyOf(differences);
    }

    protected JsonObjectLocation contextLeft(Object o) {
        return checkPresent(leftObj.getLocations().getLocationOf(o),
                "can't find left context for " + o);
    }

    protected JsonObjectLocation contextRight(Object o) {
        return checkPresent(rightObj.getLocations().getLocationOf(o),
                "can't find right context for " + o);
    }

    public JsonDiff putContexts(Object left, Object right) {
        leftContexts.put(currentLeftPath(), left);
        rightContexts.put(currentRightPath(), right);
        return this;
    }

    public JsonObjectLocation currentLeftContextLocation() {
        List<String> path = new LinkedList<>(this.currentLeftPath);
        Optional<JsonObjectLocation> l = leftObj.getLocations().getLocationOf(leftContexts.get(pathFor(path)));
        while (!l.isPresent() && path.size() > 1) {
            path.remove(path.size() - 1);
            l = leftObj.getLocations().getLocationOf(leftContexts.get(pathFor(path)));
        }
        return l.orNull();
    }

    public JsonObjectLocation currentRightContextLocation() {
        List<String> path = new LinkedList<>(this.currentRightPath);
        Optional<JsonObjectLocation> l = rightObj.getLocations().getLocationOf(rightContexts.get(pathFor(path)));
        while (!l.isPresent() && path.size() > 1) {
            path.remove(path.size() - 1);
            l = rightObj.getLocations().getLocationOf(rightContexts.get(pathFor(path)));
        }
        return l.orNull();
    }

    private String pathFor(List<String> path) {
        return Joiner.on("/").join(path);
    }

    public static interface Difference {
        public abstract String getType();
        public String getLeftPath();
        public String getRightPath();
        public JsonObjectLocation getLeftContext();
        public JsonObjectLocation getRightContext();
    }

    public static abstract class AbstractDiff implements Difference {
        private final String leftPath;
        private final String rightPath;
        private final JsonObjectLocation leftContext;
        private final JsonObjectLocation rightContext;

        protected AbstractDiff(String leftPath, String rightPath, JsonObjectLocation leftContext, JsonObjectLocation rightContext) {
            this.leftPath = leftPath;
            this.rightPath = rightPath;
            this.leftContext = leftContext;
            this.rightContext = rightContext;
        }

        public abstract String getType();

        public String getLeftPath() {
            return leftPath;
        }

        public String getRightPath() {
            return rightPath;
        }

        public JsonObjectLocation getLeftContext() {
            return leftContext;
        }

        public JsonObjectLocation getRightContext() {
            return rightContext;
        }

        @Override
        public String toString() {
            return "Diff{" +
                    "leftPath='" + leftPath + '\'' +
                    ", rightPath='" + rightPath + '\'' +
                    ", leftContext=" + leftContext +
                    ", rightContext=" + rightContext +
                    '}';
        }
    }

    public static class ValueDiff extends AbstractDiff {
        private final Object leftValue;
        private final Object rightValue;

        protected ValueDiff(String leftPath, String rightPath, JsonObjectLocation leftContext, JsonObjectLocation rightContext,
                            Object leftValue, Object rightValue) {
            super(leftPath, rightPath, leftContext, rightContext);
            this.leftValue = leftValue;
            this.rightValue = rightValue;
        }


        public String getType() {
            return "CHANGED";
        }

        public Object getLeftValue() {
            return leftValue;
        }

        public Object getRightValue() {
            return rightValue;
        }

        @Override
        public String toString() {
            return "ValueDiff{" + super.toString() +
                    ", leftValue=" + leftValue +
                    ", rightValue=" + rightValue +
                    '}';
        }
    }

    public static abstract class ArrayDiff extends AbstractDiff {
        private final int leftPosition;
        private final int rightPosition;
        private final List<Object> values;

        public ArrayDiff(String leftPath, String rightPath, JsonObjectLocation leftContext, JsonObjectLocation rightContext,
                         int leftPosition, int rightPosition, List<Object> values) {
            super(leftPath, rightPath, leftContext, rightContext);
            this.leftPosition = leftPosition;
            this.rightPosition = rightPosition;
            this.values = values;
        }

        public abstract String getType();

        public int getLeftPosition() {
            return leftPosition;
        }

        public int getRightPosition() {
            return rightPosition;
        }

        public List<Object> getValues() {
            return values;
        }

        @Override
        public String toString() {
            return "ArrayDiff{" + super.toString() +
                    ", type=" + getType() +
                    ", leftPosition=" + leftPosition +
                    ", rightPosition=" + rightPosition +
                    ", values=" + values +
                    '}';
        }
    }

    public static class ArrayInsertedValue extends ArrayDiff {
        public ArrayInsertedValue(String leftPath, String rightPath,
                                  JsonObjectLocation leftContext, JsonObjectLocation rightContext,
                                  int leftPosition, int rightPosition, List<Object> values) {
            super(leftPath, rightPath, leftContext, rightContext, leftPosition, rightPosition, values);
        }

        public String getType() {
            return "INSERTED";
        }
    }

    public static class ArrayDeletedValue extends ArrayDiff {
        public ArrayDeletedValue(String leftPath, String rightPath,
                                 JsonObjectLocation leftContext, JsonObjectLocation rightContext,
                                 int leftPosition, int rightPosition, List<Object> values) {
            super(leftPath, rightPath, leftContext, rightContext, leftPosition, rightPosition, values);
        }

        public String getType() {
            return "DELETED";
        }
    }

    public static abstract class KeyDiff extends AbstractDiff {
        private final String key;
        private final Object value;

        protected KeyDiff(String leftPath, String rightPath, JsonObjectLocation leftContext, JsonObjectLocation rightContext,
                          String key, Object value) {
            super(leftPath, rightPath, leftContext, rightContext);
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "KeyDiff{" + super.toString() +
                    ", type='" + getType() + '\'' +
                    ", key='" + key + '\'' +
                    ", value=" + value +
                    '}';
        }
    }

    public static class AddedKey extends KeyDiff {
        public AddedKey(String leftPath, String rightPath, JsonObjectLocation leftContext, JsonObjectLocation rightContext,
                        String key, Object value) {
            super(leftPath, rightPath, leftContext, rightContext, key, value);
        }

        public String getType() {
            return "ADDED";
        }
    }

    public static class RemovedKey extends KeyDiff {
        public RemovedKey(String leftPath, String rightPath, JsonObjectLocation leftContext, JsonObjectLocation rightContext,
                          String key, Object value) {
            super(leftPath, rightPath, leftContext, rightContext, key, value);
        }

        public String getType() {
            return "REMOVED";
        }
    }
}
