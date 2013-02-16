package restx.embed.simple;

import org.simpleframework.http.Response;
import restx.RestxResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * User: xavierhanin
 * Date: 2/16/13
 * Time: 1:57 PM
 */
public class SimpleRestxResponse implements RestxResponse {
    private final Response response;
    private PrintWriter writer;
    private OutputStream outputStream;

    public SimpleRestxResponse(Response response) {
        this.response = response;
    }

    @Override
    public void setStatus(int status) {
        response.setCode(status);
    }

    @Override
    public void setContentType(String s) {
        response.setValue("Content-Type", s);
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return writer = new PrintWriter(response.getPrintStream(), true);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return outputStream = response.getOutputStream();
    }

    @Override
    public void addCookie(String cookie, String value) {
        response.setCookie(cookie, value);
    }

    @Override
    public void setHeader(String headerName, String header) {
        response.setValue(headerName, header);
    }

    @Override
    public void close() throws Exception {
        if (writer != null) {
            writer.println();
            writer.close();
        }
        if (outputStream != null) {
            outputStream.close();
        }
        response.close();
    }
}
