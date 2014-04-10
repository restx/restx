package restx;

import com.google.common.collect.ImmutableMap;

/**
 * Date: 19/10/13
 * Time: 21:23
 */
public interface RestxRequestMatch {
    /**
     * The full restx path this match matched.
     * @return the full restx path this match matched.
     */
    String getPath();

    /**
     * Returns the value of a given path parameter in this match.
     *
     * This method never returns null, but rather throws an IllegalStateException. Indeed most of the time path parameters
     * are required, and there is a match only if the path param is present.
     *
     * If you want to check if a path param is present, use getPathParams() method, get the path param and check
     * for nullity.
     *
     * @param paramName the na;e of the path parameter to return
     * @return the path param value. Will never be null.
     * @throws IllegalStateException if path param is not defined.
     *
     */
    String getPathParam(String paramName);
    ImmutableMap<String, String> getPathParams();

    ImmutableMap<String, ? extends Object> getOtherParams();
}
