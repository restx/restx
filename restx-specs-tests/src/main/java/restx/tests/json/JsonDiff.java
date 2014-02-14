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
@SuppressWarnings("unchecked")
public class JsonDiff {
    private final JsonWithLocationsParser.ParsedJsonWithLocations leftObj;
    private final JsonWithLocationsParser.ParsedJsonWithLocations rightObj;

    private List<Difference> differences = new ArrayList<>();
    private List<String> currentLeftPath = new LinkedList<>(asList("."));
    private List<String> currentRightPath = new LinkedList<>(asList("."));
    private Map<String, Object> leftContexts = new LinkedHashMap<>();
    private Map<String, Object> rightContexts = new LinkedHashMap<>();
    private Map<String, Setter> leftSetters = new LinkedHashMap<>();
    private Map<String, Setter> rightSetters = new LinkedHashMap<>();

    public JsonDiff(JsonWithLocationsParser.ParsedJsonWithLocations leftObj,
                    JsonWithLocationsParser.ParsedJsonWithLocations rightObj) {
        this.leftObj = leftObj;
        this.rightObj = rightObj;

        putContexts(new NoSetter(), leftObj.getRoot(), new NoSetter(), rightObj.getRoot());
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

    public JsonDiff putContexts(Setter leftSetter, Object left, Setter rightSetter, Object right) {
        leftContexts.put(currentLeftPath(), left);
        leftSetters.put(currentLeftPath(), leftSetter);
        rightContexts.put(currentRightPath(), right);
        rightSetters.put(currentRightPath(), rightSetter);
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

    Object getLeftAt(String path) {
        return leftContexts.get(path);
    }

    private void setLeftAt(String path, Object value) {
        leftSetters.get(path).set(value);
    }

    Object getRightAt(String path) {
        return rightContexts.get(path);
    }

    private void setRightAt(String path, Object value) {
        rightSetters.get(path).set(value);
    }

    String getParentPath(String path) {
        int i = path.lastIndexOf('/');
        return i == -1 ? path : path.substring(0, i);
    }

    String getLastElementPath(String path) {
        return path.substring(path.lastIndexOf('/') + 1);
    }

    public static interface Difference {
        public abstract String getType();
        public String getLeftPath();
        public String getRightPath();
        public JsonObjectLocation getLeftContext();

        public JsonObjectLocation getRightContext();
        void mergeToRight(JsonDiff diff);
        void mergeToLeft(JsonDiff diff);

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
        public void mergeToRight(JsonDiff diff) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void mergeToLeft(JsonDiff diff) {
            throw new UnsupportedOperationException();
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

        public ValueDiff(String leftPath, String rightPath,
                         JsonObjectLocation leftContext, JsonObjectLocation rightContext,
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
        public void mergeToRight(JsonDiff diff) {
            diff.setRightAt(getRightPath(), leftValue);
        }

        @Override
        public void mergeToLeft(JsonDiff diff) {
            diff.setLeftAt(getLeftPath(), rightValue);
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
            this.values = new ArrayList<>(values);
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

        @Override
        public void mergeToRight(JsonDiff diff) {
            List l = (List) diff.getRightAt(getRightPath());
            for (int i = 0; i < getValues().size(); i++) {
                l.remove(getRightPosition());
            }
        }

        @Override
        public void mergeToLeft(JsonDiff diff) {
            List l = (List) diff.getLeftAt(getLeftPath());
            l.addAll(getLeftPosition(), getValues());
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

        @Override
        public void mergeToRight(JsonDiff diff) {
            List l = (List) diff.getRightAt(getRightPath());
            l.addAll(getRightPosition(), getValues());
        }

        @Override
        public void mergeToLeft(JsonDiff diff) {
            List l = (List) diff.getLeftAt(getLeftPath());
            for (int i = 0; i < getValues().size(); i++) {
                l.remove(getLeftPosition());
            }
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

        protected Map<String, Object> getLeftParent(JsonDiff diff) {
            return (Map<String, Object>) diff.getLeftAt(getLeftPath());
        }
        protected Map<String, Object> getRightParent(JsonDiff diff) {
            return (Map<String, Object>) diff.getRightAt(getLeftPath());
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

        @Override
        public void mergeToRight(JsonDiff diff) {
            getRightParent(diff).remove(getKey());
        }

        @Override
        public void mergeToLeft(JsonDiff diff) {
            getLeftParent(diff).put(getKey(), getValue());
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

        @Override
        public void mergeToRight(JsonDiff diff) {
            getRightParent(diff).put(getKey(), getValue());
        }

        @Override
        public void mergeToLeft(JsonDiff diff) {
            getLeftParent(diff).remove(getKey());
        }
    }

    public static interface Setter {
        void set(Object value);
    }

    public static class NoSetter  implements Setter {
        @Override
        public void set(Object value) {
            throw new UnsupportedOperationException();
        }
    }

    public static class ListSetter  implements Setter {
        private final List<Object> objects;
        private final int i;

        public ListSetter(List<Object> objects, int i) {
            this.objects = objects;
            this.i = i;
        }

        @Override
        public void set(Object value) {
            objects.set(i, value);
        }
    }

    public static class MapSetter  implements Setter {
        private final Map<String, Object> map;
        private final String key;

        public MapSetter(Map<String, Object> map, String key) {
            this.map = map;
            this.key = key;
        }

        @Override
        public void set(Object value) {
            map.put(key, value);
        }
    }
}
