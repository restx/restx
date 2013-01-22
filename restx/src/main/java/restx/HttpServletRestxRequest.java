package restx;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * User: xavierhanin
 * Date: 1/22/13
 * Time: 2:52 PM
 */
public class HttpServletRestxRequest implements RestxRequest {
    private final HttpServletRequest request;

    public HttpServletRestxRequest(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public Optional<String> getQueryParam(String param) {
        return Optional.fromNullable(request.getParameter(param));
    }

    @Override
    public List<String> getQueryParams(String param) {
        return Lists.newArrayList(request.getParameterValues(param));
    }

    @Override
    public InputStream getContentStream() throws IOException {
        return request.getInputStream();
    }
}
