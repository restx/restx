package restx.apidocs;

import restx.ResourcesRoute;
import restx.factory.Component;

/**
 * Date: 17/5/14
 * Time: 12:49
 */
@Component(priority = -100)
public class ApiDocsNotesRoute extends ResourcesRoute {
    public ApiDocsNotesRoute() {
        super("ApiDocsNotesRoute", "/@/api-docs/notes/", "apidocs");
    }
}
