package restx.entity;

import com.google.common.base.Optional;
import restx.RestxRequest;
import restx.RestxRequestMatch;

import java.io.IOException;

/**
 * User: xavierhanin
 * Date: 2/17/13
 * Time: 1:06 PM
 */
public interface MatchedEntityRoute<I, O> {
    Optional<O> route(RestxRequest restxRequest, RestxRequestMatch match, I input) throws IOException;
}
