package restx.tests.json;

/**
 * @author damienriccio
 */
public interface JsonDiffComparator {
    boolean compare(JsonDiff diff, Object o1, Object o2);
}
