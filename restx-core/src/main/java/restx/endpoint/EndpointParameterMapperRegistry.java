package restx.endpoint;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import restx.factory.Component;

@Component
public class EndpointParameterMapperRegistry {
  private final Iterable<EndpointParameterMapperFactory> factories;

  public EndpointParameterMapperRegistry(Iterable<EndpointParameterMapperFactory> factories) {
    this.factories = factories;
  }

  public <T> EndpointParameterMapper getEndpointParameterMapperFor(EndpointParameter endpointParameter) {
    for (EndpointParameterMapperFactory factory : factories) {
      Optional<? extends EndpointParameterMapper> mapper = factory.getEndpointParameterMapperFor(endpointParameter);
      if (mapper.isPresent()) {
        return mapper.get();
      }
    }

    throw new IllegalStateException(String.format(
            "no mapper found for parameter %s !%nAvailable factories: %s",
            endpointParameter,
            Iterables.toString(factories)));
  }
}