package restx.entity;

import restx.RestxContext;
import restx.RestxRequest;
import restx.RestxResponse;
import restx.http.HttpStatus;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Date: 23/10/13
 * Time: 11:39
 */
public abstract class AbstractEntityResponseWriter<T> implements EntityResponseWriter<T> {
    private final Type type;
    private final String contentType;

    protected AbstractEntityResponseWriter(Type type, String contentType) {
        this.type = type;
        this.contentType = contentType;
    }

    @Override
    public Type getType() {
        return type;
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
