package restx;

/**
 * A route to handle http requests.
 *
 * Contrary to filters, only one route is selected per http request to handle it.
 *
 * This interface has no specific methods, it's mainly a marker interface following the RestxHandler contract.
 */
public interface RestxRoute extends RestxHandler {
}
