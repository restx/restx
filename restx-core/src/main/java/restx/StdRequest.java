package restx;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.getFirst;

/**
 * User: xavierhanin
 * Date: 4/2/13
 * Time: 9:16 PM
 */
public class StdRequest implements RestxRequest {
    public static StdRequestBuilder builder() {
        return new StdRequestBuilder();
    }

    private final String baseUri;
    private final String restxPath;
    private final String httpMethod;
    private final ImmutableMap<String, String> headers;
    private final ImmutableMap<String, List<String>> queryParams;
    private final ImmutableMap<String, String> cookiesMap;
    private final Supplier<InputStream> inputStreamSupplier;
    private InputStream inputStream;

    private StdRequest(String baseUri, String restxPath, String httpMethod,
                      ImmutableMap<String, String> headers, ImmutableMap<String, List<String>> queryParams,
                      ImmutableMap<String, String> cookiesMap, Supplier<InputStream> inputStreamSupplier) {
        this.baseUri = checkNotNull(baseUri, "baseUri is required");
        this.restxPath = checkNotNull(restxPath, "restxPath is required");
        this.httpMethod = checkNotNull(httpMethod, "httpMethod is required");
        this.queryParams = checkNotNull(queryParams, "query params are required");
        this.cookiesMap = checkNotNull(cookiesMap, "cookies map is required");
        this.headers = checkNotNull(headers, "headers is required");
        this.inputStreamSupplier = checkNotNull(inputStreamSupplier, "inputstream supplier is required");
    }

    @Override
    public String getBaseUri() {
        return baseUri;
    }

    @Override
    public String getRestxPath() {
        return restxPath;
    }

    @Override
    public String getRestxUri() {
        if (queryParams.isEmpty()) {
            return restxPath;
        }
        StringBuilder sb = new StringBuilder(restxPath).append("?");
        for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
            for (String val : entry.getValue()) {
                sb.append(entry.getKey()).append("=").append(val).append("&");
            }
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    @Override
    public String getHttpMethod() {
        return httpMethod;
    }

    @Override
    public Optional<String> getQueryParam(String param) {
        return Optional.fromNullable(getFirst(getQueryParams(param), null));
    }

    @Override
    public List<String> getQueryParams(String param) {
        return Optional.fromNullable(queryParams.get(param)).or(Collections.<String>emptyList());
    }

    @Override
    public Optional<String> getHeader(String headerName) {
        return Optional.fromNullable(headers.get(headerName));
    }

    @Override
    public String getCookieValue(String cookie, String defaultValue) {
        return Optional.fromNullable(cookiesMap.get(cookie)).or(defaultValue);
    }

    @Override
    public boolean isPersistentCookie(String cookie) {
        return false;
    }

    @Override
    public Map<String, String> getCookiesMap() {
        return cookiesMap;
    }

    @Override
    public synchronized InputStream getContentStream() throws IOException {
        if (inputStream != null) {
            throw new IllegalStateException("can't get content stream multiple times");
        }
        return inputStream = inputStreamSupplier.get();
    }

    @Override
    public synchronized void closeContentStream() throws IOException {
        if (inputStream == null) {
            throw new IllegalStateException("can't close content stream which is not opened");
        }
        inputStream.close();
    }

    public static class StdRequestBuilder {
        private String baseUri;
        private String restxPath;
        private String httpMethod = "GET";
        private ImmutableMap<String, String> headers = ImmutableMap.of();
        private ImmutableMap<String, List<String>> queryParams = ImmutableMap.of();
        private ImmutableMap<String, String> cookiesMap = ImmutableMap.of();
        private Supplier<InputStream> inputStreamSupplier = new Supplier<InputStream>() {
            @Override
            public InputStream get() {
                throw new UnsupportedOperationException();
            }
        };

        public StdRequestBuilder setBaseUri(String baseUri) {
            this.baseUri = baseUri;
            return this;
        }

        public StdRequestBuilder setRestxPath(String restxPath) {
            this.restxPath = restxPath;
            return this;
        }

        public StdRequestBuilder setHttpMethod(String httpMethod) {
            this.httpMethod = httpMethod;
            return this;
        }

        public StdRequestBuilder setHeaders(ImmutableMap<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public StdRequestBuilder setQueryParams(ImmutableMap<String, List<String>> queryParams) {
            this.queryParams = queryParams;
            return this;
        }

        public StdRequestBuilder setCookiesMap(ImmutableMap<String, String> cookiesMap) {
            this.cookiesMap = cookiesMap;
            return this;
        }

        public StdRequestBuilder setInputStreamSupplier(Supplier<InputStream> inputStreamSupplier) {
            this.inputStreamSupplier = inputStreamSupplier;
            return this;
        }

        public StdRequest build() {
            return new StdRequest(baseUri, restxPath, httpMethod, headers, queryParams, cookiesMap, inputStreamSupplier);
        }
    }
}
