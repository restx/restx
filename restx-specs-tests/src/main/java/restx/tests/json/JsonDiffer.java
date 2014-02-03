package restx.tests.json;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;
import difflib.myers.Equalizer;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Date: 3/2/14
 * Time: 22:03
 */
public class JsonDiffer {
    public static class Config {
        private boolean ignoreExtraFields = false;

        public boolean isIgnoreExtraFields() {
            return ignoreExtraFields;
        }

        public Config setIgnoreExtraFields(final boolean ignoreExtraFields) {
            this.ignoreExtraFields = ignoreExtraFields;
            return this;
        }
    }

    private Config leftConfig = new Config();
    private Config rightConfig = new Config();

    public Config getLeftConfig() {
        return leftConfig;
    }

    public Config getRightConfig() {
        return rightConfig;
    }

    public JsonDiff compare(JsonSource left, JsonSource right) {
        try {
            JsonWithLocationsParser.ParsedJsonWithLocations leftObj = new JsonWithLocationsParser().parse(left, Object.class);
            JsonWithLocationsParser.ParsedJsonWithLocations rightObj = new JsonWithLocationsParser().parse(right, Object.class);

            return diff(new JsonDiff(leftObj, rightObj), leftObj.getRoot(), rightObj.getRoot());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private JsonDiff diff(JsonDiff diff, Object o1, Object o2) {
        if (o1 instanceof Map && o2 instanceof  Map) {
            diffMaps(diff, (Map) o1, (Map) o2);
        } else if (o1 instanceof List && o2 instanceof List) {
            diffLists(diff, (List) o1, (List) o2);
        } else {
            if (!Objects.equal(o1, o2)) {
                diff.addDifference(new JsonDiff.ValueDiff(
                        diff.currentLeftPath(),
                        diff.currentRightPath(),
                        diff.currentLeftContextLocation(),
                        diff.currentRightContextLocation(),
                        o1, o2
                ));
            }
        }
        return diff;
    }

    private void diffLists(final JsonDiff diff, List<Object> o1, List<Object> o2) {
        final Patch<Object> lDiff = DiffUtils.diff(o1, o2, new Equalizer<Object>() {
            @Override
            public boolean equals(Object o1, Object o2) {
                // we don't give new roots here, since we only need to know if objects are the the same
                return diff(new JsonDiff(diff.getLeftObj(), diff.getRightObj()), o1, o2).isSame();
            }
        });
        for (Delta<Object> objectDelta : lDiff.getDeltas()) {
            switch (objectDelta.getType()) {
                case INSERT:
                    diff.addDifference(new JsonDiff.ArrayInsertedValue(
                            diff.currentLeftPath(),
                            diff.currentRightPath(),
                            diff.currentLeftContextLocation(),
                            diff.currentRightContextLocation(),
                            objectDelta.getOriginal().getPosition(),
                            objectDelta.getRevised().getPosition(),
                            objectDelta.getRevised().getLines()
                    ));
                    break;
                case DELETE:
                    diff.addDifference(new JsonDiff.ArrayDeletedValue(
                            diff.currentLeftPath(),
                            diff.currentRightPath(),
                            diff.currentLeftContextLocation(),
                            diff.currentRightContextLocation(),
                            objectDelta.getOriginal().getPosition(),
                            objectDelta.getRevised().getPosition(),
                            objectDelta.getOriginal().getLines()
                    ));
                    break;
                case CHANGE:
                    int leftPosition = objectDelta.getOriginal().getPosition();
                    int rightPosition = objectDelta.getRevised().getPosition();
                    int changed = Math.min(
                            objectDelta.getOriginal().getLines().size(),
                            objectDelta.getRevised().getLines().size());
                    for (int i = 0; i < changed; i++) {
                        Object left = o1.get(leftPosition + i);
                        Object right = o2.get(rightPosition + i);

                        try {
                            diff(diff.goIn("["+(leftPosition + i)+"]", "["+(rightPosition + i)+"]")
                                    .putContexts(left, right), left, right);
                        } finally {
                            diff.goUp();
                        }
                    }
                    // we may have ore values in either of two lists:
                    // diff algo is not returning INSERT / DELETE in some cases
                    if (objectDelta.getOriginal().getLines().size() > changed) {
                        diff.addDifference(new JsonDiff.ArrayDeletedValue(
                                diff.currentLeftPath(),
                                diff.currentRightPath(),
                                diff.currentLeftContextLocation(),
                                diff.currentRightContextLocation(),
                                leftPosition + changed,
                                rightPosition + changed,
                                o1.subList(leftPosition + changed,
                                        leftPosition + objectDelta.getOriginal().getLines().size())
                        ));
                    }
                    if (objectDelta.getRevised().getLines().size() > changed) {
                        diff.addDifference(new JsonDiff.ArrayInsertedValue(
                                diff.currentLeftPath(),
                                diff.currentRightPath(),
                                diff.currentLeftContextLocation(),
                                diff.currentRightContextLocation(),
                                leftPosition + changed,
                                rightPosition + changed,
                                o2.subList(rightPosition + changed,
                                        rightPosition + objectDelta.getRevised().getLines().size())
                        ));
                    }
                    break;
            }
        }
    }

    private void diffMaps(JsonDiff diff, Map<String, Object> m1, Map<String, Object> m2) {
        if (!leftConfig.isIgnoreExtraFields()) {
            for (String k : Sets.difference(m1.keySet(), m2.keySet())) {
                diff.addDifference(new JsonDiff.RemovedKey(
                        diff.currentLeftPath(),
                        diff.currentRightPath(),
                        diff.contextLeft(m1),
                        diff.contextRight(m2),
                        k, m1.get(k)));
            }
        }
        if (!rightConfig.isIgnoreExtraFields()) {
            for (String k : Sets.difference(m2.keySet(), m1.keySet())) {
                diff.addDifference(new JsonDiff.AddedKey(
                        diff.currentLeftPath(),
                        diff.currentRightPath(),
                        diff.contextLeft(m1),
                        diff.contextRight(m2),
                        k, m2.get(k)));
            }
        }
        for (String k : Sets.intersection(m1.keySet(), m2.keySet())) {
            try { diff(diff.goIn(k).putContexts(m1.get(k), m2.get(k)), m1.get(k), m2.get(k)); } finally { diff.goUp(); }
        }
    }
}
