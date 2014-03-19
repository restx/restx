package restx.tests.json;

/**
 * @author damienriccio
 * @date 3/19/14
 */
public interface JsonDiffComparator {
    boolean compare(JsonDiff diff, Object o1, Object o2);
}
