package restx;

import com.jamonapi.MonitorFactory;
import restx.factory.BoundlessComponentBox;
import restx.factory.Name;
import restx.factory.SingleNameFactoryMachine;

import java.io.IOException;

/**
 * User: xavierhanin
 * Date: 2/9/13
 * Time: 12:44 AM
 */
public class MonitorRoute implements RestxRoute {
    @Override
    public boolean route(RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
        if ("GET".equals(req.getHttpMethod()) && "/@/monitor".equals(req.getRestxPath())) {
            resp.setContentType("text/html");

            resp.getWriter().println(MonitorFactory.getReport());
            return true;
        }

        return false;
    }

    public static class Factory extends SingleNameFactoryMachine<MonitorRoute> {

        public Factory() {
            super(0, Name.of(MonitorRoute.class, "MonitorRoute"), BoundlessComponentBox.FACTORY);
        }

        @Override
        protected MonitorRoute doNewComponent(restx.factory.Factory factory) {
            return new MonitorRoute();
        }
    }
}
