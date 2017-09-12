package restx;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Date: 15/11/13
 * Time: 18:38
 */
public abstract class AbstractRequest implements RestxRequest {
    protected final HttpSettings httpSettings;

    protected AbstractRequest(HttpSettings httpSettings) {
        this.httpSettings = httpSettings;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[RESTX REQUEST] ");
        sb.append(getHttpMethod()).append(" ").append(getRestxPath());
        dumpParameters(sb);
        return sb.toString();
    }

    private void dumpParameters(StringBuilder sb) {
        ImmutableMap<String,ImmutableList<String>> queryParams = getQueryParams();
        if (queryParams.isEmpty()) {
            return;
        }
        sb.append(" ? ");
        for (Map.Entry<String, ImmutableList<String>> entry : queryParams.entrySet()) {
            String key = entry.getKey();
            List<String> values = entry.getValue();
            sb.append(key).append("=").append(
                    values.size() == 1
                            ? values.get(0)
                            : Joiner.on("&" + key + "=").join(values));
            sb.append("&");
        }
        sb.setLength(sb.length() - 1);
    }

    @Override
    public String getBaseUri() {
        return getScheme() + ":" + getBaseNetworkPath();
    }

    @Override
    public String getBaseNetworkPath() {
        checkProxyRequest();
        return "//" + getHost() + getBaseApiPath();
    }

    protected String getHost() {
        Optional<String> host = httpSettings.host();
        if (host.isPresent()) {
            return host.get();
        }

        Optional<String> forwardedHost = getHeader("X-Forwarded-Host");
        if (forwardedHost.isPresent()) {
            return Iterables.getFirst(Splitter.on(",").trimResults().split(forwardedHost.get()),
                    getHeader("Host").or(""));
        } else {
            return getHeader("Host").or("");
        }
    }

    @Override
    public boolean isSecured() {
        checkProxyRequest();
        return getScheme().equalsIgnoreCase("https");
    }

    protected String getScheme() {
        Optional<String> proto = httpSettings.scheme().or(getHeader("X-Forwarded-Proto"));
        if (proto.isPresent()) {
            return proto.get();
        }
        Optional<String> via = getHeader("Via");
        if (via.isPresent()) {
            boolean secured = via.get().toUpperCase(Locale.ENGLISH).startsWith("HTTPS");
            return secured ? "https" : "http";
        } else {
            return getLocalScheme();
        }
    }

    @Override
    public String getClientAddress() {
        // see http://en.wikipedia.org/wiki/X-Forwarded-For
        checkProxyRequest();
        Optional<String> xff = getHeader("X-Forwarded-For");
        if (xff.isPresent()) {
            return Iterables.getFirst(Splitter.on(",").trimResults().split(xff.get()),
                    getLocalClientAddress());
        } else {
            return getLocalClientAddress();
        }
    }

    protected void checkProxyRequest() {
        if (getHeader("X-Forwarded-Proto").isPresent()) {
            String localClientAddress = getLocalClientAddress();
            Collection<String> forwardedSupport = httpSettings.forwardedSupport();
            if (!forwardedSupport.contains("all")
                    && !forwardedSupport.contains(localClientAddress)) {
                throw new IllegalArgumentException(
                        "Unauthorized proxy request from " + localClientAddress + "\n" +
                                "If you are the application developer or operator, you can set `restx.http.XForwardedSupport`\n" +
                                "system property to allow proxy requests from this proxy IP with:\n" +
                                "  -Drestx.http.XForwardedSupport=" + localClientAddress + "\n" +
                                "Or if you want to allow any proxy request:\n" +
                                "  -Drestx.http.XForwardedSupport=all");
            }
        }
    }

    /**
     * Returns the client address of this request, without taking proxy into account
     * @return the client address of this request, without taking proxy into account
     */
    protected abstract String getLocalClientAddress();


    /**
     * The path on which restx is mounted.
     * Eg /api
     *
     * @return the path on which restx is mounted.
     */
    protected abstract String getBaseApiPath();

    /**
     * The URL scheme used for this request, without taking proxy into account.
     * Eg: http, https
     *
     * @return URL scheme used for this request, without taking proxy into account.
     */
    protected abstract String getLocalScheme();

}
