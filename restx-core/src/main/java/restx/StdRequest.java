package restx;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.getFirst;

/**
 * User: xavierhanin
 * Date: 4/2/13
 * Time: 9:16 PM
 */
public class StdRequest extends AbstractRequest {

    public static StdRequestBuilder builder() {
        return new StdRequestBuilder();
    }

    private final int port;
    private final String baseUri;
    private final String restxPath;
    private final String httpMethod;
    private final ImmutableMap<String, String> headers;
    private final ImmutableMap<String, ImmutableList<String>> queryParams;
    private final ImmutableMap<String, String> cookiesMap;
    private final Supplier<InputStream> inputStreamSupplier;
    private InputStream inputStream;

    private StdRequest(String baseUri, String restxPath, String httpMethod,
                      ImmutableMap<String, String> headers, ImmutableMap<String, ImmutableList<String>> queryParams,
                      ImmutableMap<String, String> cookiesMap, Supplier<InputStream> inputStreamSupplier) {
        this.baseUri = checkNotNull(baseUri, "baseUri is required");
        this.restxPath = checkNotNull(restxPath, "restxPath is required");
        this.httpMethod = checkNotNull(httpMethod, "httpMethod is required");
        this.queryParams = checkNotNull(queryParams, "query params are required");
        this.cookiesMap = checkNotNull(cookiesMap, "cookies map is required");
        this.headers = checkNotNull(headers, "headers is required");
        this.inputStreamSupplier = checkNotNull(inputStreamSupplier, "inputstream supplier is required");
        this.port = getPortFromBaseUri(baseUri);
    }

    private int getPortFromBaseUri(String baseUri) {
        try {
            return new URI(baseUri).getPort();
        } catch (URISyntaxException e) {
            return 80;
        }
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String getBaseUri() {
        return baseUri;
    }

    @Override
    public String getBaseNetworkPath() {
        return getBaseUri().replaceAll("^https?:", "");
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
        for (Map.Entry<String, ImmutableList<String>> entry : queryParams.entrySet()) {
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
    public ImmutableList<String> getQueryParams(String param) {
        return Optional.fromNullable(queryParams.get(param)).or(ImmutableList.<String>of());
    }

    public ImmutableMap<String, ImmutableList<String>> getQueryParams() {
        return queryParams;
    }

    @Override
    public Optional<String> getHeader(String headerName) {
        return Optional.fromNullable(headers.get(headerName));
    }

    @Override
    public String getContentType() {
        return headers.get("Content-type");
    }

    @Override
    public Optional<String> getCookieValue(String cookieName) {
        return Optional.fromNullable(cookiesMap.get(cookieName));
    }

    @Override
    public boolean isPersistentCookie(String cookie) {
        return false;
    }

    @Override
    public ImmutableMap<String, String> getCookiesMap() {
        return cookiesMap;
    }

    @Override
    public String getClientAddress() {
        return "";
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

    @Override
    public <T> T unwrap(Class<T> clazz) {
        throw new IllegalArgumentException("no underlying implementation");
    }

    @Override
    public Locale getLocale() {
        final String acceptLanguage = headers.get("Accept-Language");

        final ArrayList<String> languagesList = new ArrayList<>();

        if (acceptLanguage != null && !acceptLanguage.isEmpty()) {
            final String[] split = acceptLanguage.replace(" ", "").replace(";", ",").split(",");

            for (String s1 : split) {
                if (!s1.startsWith("q=")) {
                    languagesList.add(s1);
                }
            }
        }

        Locale locale;

        if (languagesList.isEmpty()) {
            locale = Locale.getDefault();
        } else {
            locale = Locale.forLanguageTag(languagesList.get(0));
        }

        return locale;
    }

    public static class StdRequestBuilder {
        private String baseUri;
        private String restxPath;
        private String httpMethod = "GET";
        private ImmutableMap<String, String> headers = ImmutableMap.of();
        private ImmutableMap<String, ImmutableList<String>> queryParams = ImmutableMap.of();
        private ImmutableMap<String, String> cookiesMap = ImmutableMap.of();
        private Supplier<InputStream> inputStreamSupplier = new Supplier<InputStream>() {
            @Override
            public InputStream get() {
                throw new UnsupportedOperationException();
            }
        };

        /**
         * Sets a full restx path for the request, properly handling the query string.
         *
         * So calling <code>setFullPath("/message?who=xavier")</code> is equivalent to
         * <code>setRestxPath("/message").setQueryParams(ImmutableMap.of("who", "xavier"))</code>.
         *
         * @param fullPath the full path.
         * @return the current builder.
         */
        public StdRequestBuilder setFullPath(String fullPath) {
            int queryStringIndex = fullPath.indexOf('?');
            if (queryStringIndex == -1) {
                restxPath = fullPath;
                return this;
            }

            restxPath = fullPath.substring(0, queryStringIndex);

            Map<String, List<String>> params = Maps.newLinkedHashMap();
            for (String queryParam : Splitter.on("&").split(fullPath.substring(queryStringIndex + 1))) {
                String param = queryParam;
                String val = "";
                int eqIndex = queryParam.indexOf('=');
                if (eqIndex != -1) {
                    param = queryParam.substring(0, eqIndex);
                    val = queryParam.substring(eqIndex + 1);
                }

                List<String> paramValues = params.get(param);
                if (paramValues == null) {
                    params.put(param, paramValues = Lists.<String>newArrayList());
                }
                paramValues.add(val);
            }

            ImmutableMap.Builder<String, ImmutableList<String>> paramsBuilder = ImmutableMap.builder();
            for (Map.Entry<String, List<String>> entry : params.entrySet()) {
                paramsBuilder.put(entry.getKey(), ImmutableList.copyOf(entry.getValue()));
            }
            queryParams = paramsBuilder.build();

            return this;
        }

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

        public StdRequestBuilder setQueryParams(ImmutableMap<String, ImmutableList<String>> queryParams) {
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
