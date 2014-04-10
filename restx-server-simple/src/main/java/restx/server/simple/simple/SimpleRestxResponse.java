package restx.server.simple.simple;

import org.joda.time.Duration;
import org.simpleframework.http.Cookie;
import org.simpleframework.http.Response;
import restx.AbstractResponse;
import restx.RestxResponse;
import restx.http.HttpStatus;

import java.io.IOException;
import java.io.OutputStream;

/**
 * User: xavierhanin
 * Date: 2/16/13
 * Time: 1:57 PM
 */
public class SimpleRestxResponse extends AbstractResponse<Response> {
    private final Response response;

    public SimpleRestxResponse(Response response) {
        super(Response.class, response);
        this.response = response;
    }

    @Override
    public RestxResponse addCookie(String cookie, String value, Duration expiration) {
        Cookie c = new Cookie(cookie, value, "/");
        c.setExpiry(expiration.getStandardSeconds() > 0 ? (int) expiration.getStandardSeconds() : -1);
        response.setCookie(c);
        return this;
    }

    @Override
    public RestxResponse clearCookie(String cookie) {
        Cookie c = new Cookie(cookie, "");
        c.setPath("/");
        c.setExpiry(0);
        response.setCookie(c);
        return this;
    }

    @Override
    public RestxResponse setHeader(String headerName, String header) {
        response.setValue(headerName, header);
        return this;
    }

    @Override
    protected void closeResponse() throws IOException {
        response.close();
    }

    @Override
    protected OutputStream doGetOutputStream() throws IOException {
        return response.getOutputStream();
    }

    @Override
    protected void doSetStatus(HttpStatus httpStatus) {
        response.setCode(httpStatus.getCode());
    }
}
