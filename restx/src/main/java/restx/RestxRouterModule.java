package restx;

/**
 * User: xavierhanin
 * Date: 1/18/13
 * Time: 11:32 PM
 */
public abstract class RestxRouterModule {
    public abstract Class<? extends RestxRoute> router();
}
