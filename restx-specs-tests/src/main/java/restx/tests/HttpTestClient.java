package restx.tests;

import com.github.kevinsawicki.http.HttpRequest;
import restx.factory.Factory;

/**
 * HttpTestClient is a helper to create com.github.kevinsawicki.http.HttpRequest
 * which sets the RestxThreadLocal header to share client thread local components with server one
 * (when used with a per request factory loading).
 */
public class HttpTestClient {
    public static HttpRequest http(String method, String url) {
        return new HttpRequest(url, method)
                .header("RestxThreadLocal", Factory.LocalMachines.threadLocal().getId());
    }

    public static HttpRequest GET(String url) {
        return http("GET", url);
    }

    public static HttpRequest HEAD(String url) {
        return http("HEAD", url);
    }

    public static HttpRequest OPTIONS(String url) {
        return http("OPTIONS", url);
    }

    public static HttpRequest POST(String url) {
        return http("POST", url);
    }

    public static HttpRequest PUT(String url) {
        return http("PUT", url);
    }

    public static HttpRequest DELETE(String url) {
        return http("DELETE", url);
    }

}
