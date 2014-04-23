package restx.common.metrics.api;

public interface MetricRegistry {
    Timer timer(String name);
}
