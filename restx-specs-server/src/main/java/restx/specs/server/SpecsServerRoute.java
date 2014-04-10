package restx.specs.server;

import com.google.common.base.Optional;
import restx.RestxContext;
import restx.RestxHandler;
import restx.RestxHandlerMatch;
import restx.RestxRequest;
import restx.RestxRequestMatch;
import restx.RestxResponse;
import restx.RestxRoute;
import restx.StdRestxRequestMatch;
import restx.factory.Component;
import restx.http.HttpStatus;
import restx.specs.RestxSpec;
import restx.specs.RestxSpecRepository;
import restx.specs.When;
import restx.specs.WhenHttpRequest;

import java.io.IOException;
import java.util.Iterator;

/**
 * User: xavierhanin
 * Date: 4/10/13
 * Time: 12:04 PM
 */
@Component(priority = 1000)
public class SpecsServerRoute implements RestxRoute {
    private final RestxSpecRepository specRepository;

    public SpecsServerRoute(RestxSpecRepository specRepository) {
        this.specRepository = specRepository;
    }

    @Override
    public Optional<RestxHandlerMatch> match(RestxRequest req) {
        Iterable<WhenHttpRequest> spec = specRepository.findSpecsByRequest(req);
        Iterator<WhenHttpRequest> iterator = spec.iterator();
        if (!iterator.hasNext()) {
            return Optional.absent();
        }

        final WhenHttpRequest whenHttpRequest = iterator.next();
        return Optional.of(new RestxHandlerMatch(
                new StdRestxRequestMatch(req.getRestxPath()),
                new RestxHandler() {
                    @Override
                    public void handle(RestxRequestMatch match, RestxRequest req, RestxResponse resp,
                                       RestxContext ctx) throws IOException {
                        resp.setStatus(HttpStatus.havingCode(whenHttpRequest.getThen().getExpectedCode()));
                        if (whenHttpRequest.getThen().getExpectedCode() == HttpStatus.OK.getCode()) {
                            resp.setContentType("application/json");
                        }
                        resp.getWriter().print(whenHttpRequest.getThen().getExpected());
                    }
                }
        ));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("-- routes defined by specs:\n");
        for (String spec : specRepository.findAll()){
            Optional<RestxSpec> s = specRepository.findSpecById(spec);
            for (When when : s.get().getWhens()) {
                if (when instanceof WhenHttpRequest) {
                    WhenHttpRequest httpRequest = (WhenHttpRequest) when;
                    sb.append(httpRequest.getMethod()).append(" ").append(httpRequest.getPath())
                            .append(" (").append(spec).append(")\n");
                }
            }
        }
        sb.append("--");

        return sb.toString();
    }
}
