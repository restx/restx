package restx.common.metrics.api;

public interface MetricRegistry {
//    public MetricRegistry newMetricRegistry(){
//        return new MetricRegistry();
//    };
    Timer timer(String name);
}
