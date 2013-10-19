package restx;

/**
 * A filter to handle http requests.
 *
 * This concept is very similar to servlet filters, a filter can be used to pre process or post process requests
 * handled by a route.
 *
 * This interface has no specific methods, it's mainly a marker interface following the RestxHandler contract.
 */
public interface RestxFilter extends RestxHandlerMatcher {
}
