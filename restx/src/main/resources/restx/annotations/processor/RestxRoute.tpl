    @Provides @Named("{routeId}")
    RestxRoute provide{resource}{routeName}Route(final {resource} resource, final ObjectMapper mapper) {
        return new StdRoute("{routeId}", mapper, new StdRouteMatcher("{method}", "{path}")) {
            @Override
            protected Optional<?> doRoute(RestxRouteMatch match) {
                return resource.{call};
            }
        };
    }
