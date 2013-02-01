        new StdRoute("{routeId}", mapper, new StdRouteMatcher("{method}", "{path}")) {
            @Override
            protected Optional<?> doRoute(RestxRequest request, RestxRouteMatch match) throws IOException {
                return {call};
            }

            {overrideWriteValue}
        }