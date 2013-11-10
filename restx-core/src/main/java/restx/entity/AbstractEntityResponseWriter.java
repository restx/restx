package restx.entity;

import restx.RestxContext;
import restx.RestxRequest;
import restx.RestxResponse;
import restx.http.HttpStatus;

import java.io.IOException;

/**
 * Date: 23/10/13
 * Time: 11:39
 */
public abstract class AbstractEntityResponseWriter<T> implements EntityResponseWriter<T> {
    private final String contentType;

    protected AbstractEntityResponseWriter(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public void sendResponse(HttpStatus status, T value, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
        resp.setStatus(status);
        resp.setContentType(contentType);
        writeHeaders(value, req, resp, ctx);
        ctx.getLifecycleListener().onBeforeWriteContent(req, resp);
        write(value, req, resp, ctx);
        ctx.getLifecycleListener().onAfterWriteContent(req, resp);

    }

    protected void writeHeaders(T value, RestxRequest req, RestxResponse resp, RestxContext ctx) {

    }

    abstract protected void write(T value, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException;
}
