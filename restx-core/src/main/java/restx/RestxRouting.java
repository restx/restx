package restx;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * User: xavierhanin
 * Date: 4/1/13
 * Time: 11:28 PM
 */
public class RestxRouting {
    private final ImmutableList<RestxFilter> filters;
    private final ImmutableList<RestxRoute> routes;

    public RestxRouting(ImmutableList<RestxFilter> filters, ImmutableList<RestxRoute> routes) {
        this.filters = filters;
        this.routes = routes;
    }

    public ImmutableList<RestxFilter> getFilters() {
        return filters;
    }

    public ImmutableList<RestxRoute> getRoutes() {
        return routes;
    }

    public Optional<Match> match(RestxRequest restxRequest) {
        List<RestxHandlerMatch> matches = Lists.newArrayListWithCapacity(filters.size() + 1);
        for (RestxFilter filter : filters) {
            Optional<? extends RestxHandlerMatch> match = filter.match(restxRequest);
            if (match.isPresent()) {
                matches.add(match.get());
            }
        }
        for (RestxRoute route : routes) {
            Optional<? extends RestxHandlerMatch> match = route.match(restxRequest);
            if (match.isPresent()) {
                matches.add(match.get());
                return Optional.of(new Match(matches, match));
            }
        }
        return Optional.absent();
    }

    public static class Match {
        private final List<RestxHandlerMatch> matches;
        private final Optional<? extends RestxHandlerMatch> match;

        private Match(List<RestxHandlerMatch> matches, Optional<? extends RestxHandlerMatch> match) {
            this.matches = matches;
            this.match = match;
        }

        public List<RestxHandlerMatch> getMatches() {
            return matches;
        }

        public Optional<? extends RestxHandlerMatch> getMatch() {
            return match;
        }

        @Override
        public String toString() {
            return "Match{" +
                    "matches=" + matches +
                    ", match=" + match +
                    '}';
        }
    }
}
