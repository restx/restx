package restx;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * User: xavierhanin
 * Date: 1/19/13
 * Time: 12:00 AM
 */
public interface RestxRoute {
    boolean route(HttpServletRequest req, HttpServletResponse resp) throws IOException;
}
