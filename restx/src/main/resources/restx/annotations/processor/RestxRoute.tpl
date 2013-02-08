        new StdRoute("{routeId}", mapper, new StdRouteMatcher("{method}", "{path}")) {
            @Override
            protected Optional<?> doRoute(RestxRequest request, RestxRouteMatch match) throws IOException {
                return {call};
            }

            {overrideWriteValue}

            @Override
            protected void describeOperation(OperationDescription operation) {
                {parametersDescription}

                operation.responseClass = "{responseClass}";
            }
        }