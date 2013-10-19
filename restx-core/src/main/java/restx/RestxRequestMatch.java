package restx;

import com.google.common.collect.ImmutableMap;

import java.io.IOException;

/**
 * Date: 19/10/13
 * Time: 21:23
 */
public interface RestxRequestMatch {
    String getPath();

    ImmutableMap<String, String> getPathParams();

    ImmutableMap<String, ? extends Object> getOtherParams();
}
