package restx.http;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import restx.HttpSettings;
import restx.RestxContext;
import restx.RestxFilter;
import restx.RestxHandler;
import restx.RestxHandlerMatch;
import restx.RestxRequest;
import restx.RestxRequestMatch;
import restx.RestxRequestMatcher;
import restx.RestxResponse;
import restx.RestxResponseWrapper;
import restx.StdRestxRequestMatcher;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

/**
 * A filter to automatically gzip responses when supported by the client.
 *
 * You need to @Provide it to enable it, it's not activated by default.
 */
public class GzipFilter implements RestxFilter, RestxHandler {
    private final ImmutableCollection<RestxRequestMatcher> matchers;

    public GzipFilter(HttpSettings httpSettings) {
        ImmutableList.Builder<RestxRequestMatcher> builder = ImmutableList.builder();
        for (String path : httpSettings.gzipPaths()) {
            builder.add(new StdRestxRequestMatcher("GET", path));
        }

        matchers = builder.build();
    }

    @Override
    public Optional<RestxHandlerMatch> match(RestxRequest request) {
        Optional<String> enc = request.getHeader("Accept-Encoding");
        if (!enc.isPresent()) {
            return Optional.absent();
        }

        if (!acceptsGzip(enc.get())) {
            return Optional.absent();
        }

        for (RestxRequestMatcher matcher : matchers) {
            Optional<? extends RestxRequestMatch> match = matcher.match(request.getHttpMethod(), request.getRestxPath());
            if (match.isPresent()) {
                return Optional.of(new RestxHandlerMatch(match.get(), this));
            }
        }

        return Optional.absent();
    }

    protected boolean acceptsGzip(String acceptsEncoding) {
        if ("*".equals(acceptsEncoding)) {
            return true;
        }
        for (String s : acceptsEncoding.split(",")) {
            if (s.trim().startsWith("gzip")) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void handle(RestxRequestMatch restxRequestMatch, RestxRequest restxRequest, RestxResponse restxResponse,
                       RestxContext restxContext) throws IOException {
        try {
            restxContext.nextHandlerMatch().handle(restxRequest, new RestxResponseWrapper(restxResponse) {
                private GZIPOutputStream gzipOutputStream;

                @Override
                public OutputStream getOutputStream() throws IOException {
                    setHeader("Content-Encoding", "gzip");
                    return gzipOutputStream = new GZIPOutputStream(super.getOutputStream());
                }

                @Override
                public void close() throws Exception {
                    if (gzipOutputStream != null) {
                        try {
                            gzipOutputStream.close();
                        } finally {
                            gzipOutputStream = null;
                        }
                    }
                    super.close();
                }
            }, restxContext);
        } finally {

        }
    }
}
