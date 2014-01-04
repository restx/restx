package restx;

import com.google.common.base.Optional;
import restx.entity.StdEntityRoute;

/**
 * User: xavierhanin
 * Date: 1/30/13
 * Time: 10:12 PM
 */
public interface RouteLifecycleListener {
    public static final RouteLifecycleListener DEAF = new AbstractRouteLifecycleListener() {};

    /**
     * Called when the route that will handle the request is matched.
     *
     * @param route the matched route
     * @param req the processed request
     * @param resp the upcoming response
     */
    void onRouteMatch(RestxRoute route, RestxRequest req, RestxResponse resp);

    /**
     * Called when an entity route has transformed the request body into an entity object.
     *
     * This is not called on requests which are not processed as an entity route.
     *
     * @param route the route processing the request, which most of the time is a StdEntityRoute
     * @param req the processed request
     * @param resp the upcoming response
     * @param input the request content unmarshalled into an entity
     */
    void onEntityInput(RestxRoute route, RestxRequest req, RestxResponse resp, Optional<?> input);

    /**
     * Called when an entity route has done its job and returned the output as an entity, which has yet to be
     * marshalled in the response content stream.
     *
     * This is not called on requests which are not processed as an entity route.
     *
     * @param route the route processing the request, which most of the time is a StdEntityRoute
     * @param req the processed request
     * @param resp the upcoming response
     * @param input the request content unmarshalled into an entity
     * @param output the returned output
     */
    void onEntityOutput(RestxRoute route, RestxRequest req, RestxResponse resp, Optional<?> input, Optional<?> output);

    /**
     * Called just before the response is written.
     *
     * It's a good way to enhance the response with additional headers.
     *
     * @param req the processed request
     * @param resp the upcoming response
     */
    void onBeforeWriteContent(RestxRequest req, RestxResponse resp);

    /**
     * Called after the response is written.
     *
     * @param req the processed request
     * @param resp the upcoming response
     */
    void onAfterWriteContent(RestxRequest req, RestxResponse resp);
}
