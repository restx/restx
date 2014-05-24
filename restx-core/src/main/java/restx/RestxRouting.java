package restx;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import restx.factory.Factory;
import restx.factory.NamedComponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static com.google.common.collect.Iterables.transform;

/**
 * User: xavierhanin
 * Date: 4/1/13
 * Time: 11:28 PM
 */
public class RestxRouting {
    private final ImmutableList<NamedComponent<RestxFilter>> filters;
    private final ImmutableMultimap<RestxRoute, NamedComponent<RestxHandlerMatch>> routeFilters;
    private final ImmutableList<RestxRoute> routes;

    public RestxRouting(ImmutableList<NamedComponent<RestxFilter>> filters,
                        ImmutableList<NamedComponent<RestxRouteFilter>> routeFilters,
                        ImmutableList<RestxRoute> routes) {
        this.filters = filters;
        Builder<RestxRoute, NamedComponent<RestxHandlerMatch>> builder = ImmutableListMultimap.builder();
        for (RestxRoute route : routes) {
            for (NamedComponent<RestxRouteFilter> routeFilter : routeFilters) {
                Optional<RestxHandlerMatch> m = routeFilter.getComponent().match(route);
                if (m.isPresent()) {
                    RestxHandlerMatch restxHandlerMatch = m.get();
                    builder.put(route, NamedComponent.of(
                            RestxHandlerMatch.class, routeFilter.getName().getName(),
                            routeFilter.getPriority(), restxHandlerMatch));
                }
            }
        }
        this.routeFilters = builder.build();
        this.routes = routes;
    }

    public ImmutableList<RestxFilter> getFilters() {
        return ImmutableList.copyOf(transform(filters, NamedComponent.<RestxFilter>toComponent()));
    }

    public ImmutableCollection<? extends RestxHandlerMatch> getRouteFilters(RestxRoute route) {
        return ImmutableList.copyOf(transform(
                routeFilters.get(route), NamedComponent.<RestxHandlerMatch>toComponent()));
    }

    public ImmutableList<RestxRoute> getRoutes() {
        return routes;
    }

    public Optional<Match> match(RestxRequest restxRequest) {
        for (RestxRoute route : routes) {
            Optional<? extends RestxHandlerMatch> match = route.match(restxRequest);
            if (match.isPresent()) {
                // here we need to:
                // - check which filters apply
                // - order all filters (route filters and regular filters) by priority
                // so we put all matches as NamedComponents (to preserve the filter priority) in a list,
                // and finally sort the list by priority before returning it as a Match
                ImmutableCollection<NamedComponent<RestxHandlerMatch>> routeFilters = this.routeFilters.get(route);

                List<NamedComponent<RestxHandlerMatch>> matches = Lists.newArrayListWithCapacity(
                        filters.size() + routeFilters.size() + 1);
                matches.addAll(routeFilters);

                for (NamedComponent<RestxFilter> filter : filters) {
                    Optional<? extends RestxHandlerMatch> filterMatch = filter.getComponent().match(restxRequest);
                    if (filterMatch.isPresent()) {
                        matches.add(NamedComponent.of(
                                RestxHandlerMatch.class, filter.getName().getName(),
                                filter.getPriority(), filterMatch.get()));
                    }
                }

                return Optional.of(new Match(
                        ImmutableList.<RestxHandlerMatch>builder()
                                .addAll(
                                        transform(Ordering.from(Factory.NAMED_COMPONENT_COMPARATOR).sortedCopy(matches),
                                                NamedComponent.<RestxHandlerMatch>toComponent()))
                                .add(match.get())
                                .build(),
                        match));
            }
        }
        return Optional.absent();
    }

    public static class Match {
        private final ImmutableList<RestxHandlerMatch> matches;
        private final Optional<? extends RestxHandlerMatch> match;

        private Match(ImmutableList<RestxHandlerMatch> matches, Optional<? extends RestxHandlerMatch> match) {
            this.matches = matches;
            this.match = match;
        }

        public ImmutableList<RestxHandlerMatch> getMatches() {
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
