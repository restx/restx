package restx;

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * User: xavierhanin
 * Date: 2/6/13
 * Time: 9:46 PM
 */
public interface RestxResponse {
    void setStatus(int i);

    void setContentType(String s);

    PrintWriter getWriter() throws IOException;

    OutputStream getOutputStream() throws IOException;

    void addCookie(Cookie cookie);

    void setHeader(String headerName, String header);
}
