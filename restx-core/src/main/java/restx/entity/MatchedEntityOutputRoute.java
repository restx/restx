package restx.entity;

import com.google.common.base.Optional;
import restx.RestxRequest;
import restx.RestxRequestMatch;

import java.io.IOException;

/**
 * Date: 26/11/13
 * Time: 23:16
 */
public abstract class MatchedEntityOutputRoute<O> implements MatchedEntityRoute<Void, O> {
    @Override
    public Optional<O> route(RestxRequest restxRequest, RestxRequestMatch match, Void input) throws IOException {
        return route(restxRequest, match);
    }

    protected abstract Optional<O> route(RestxRequest restxRequest, RestxRequestMatch match) throws IOException;
}
