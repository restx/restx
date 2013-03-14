package restx;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * User: xavierhanin
 * Date: 2/6/13
 * Time: 9:46 PM
 */
public interface RestxResponse extends AutoCloseable {
    void setStatus(int i);

    void setContentType(String s);

    PrintWriter getWriter() throws IOException;

    OutputStream getOutputStream() throws IOException;

    void addCookie(String cookie, String value);
    void clearCookie(String cookie);

    void setHeader(String headerName, String header);

}
