package restx.endpoint;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import restx.endpoint.mappers.EndpointParameterMapper;
import restx.endpoint.mappers.EndpointParameterMapperFactory;
import restx.factory.Component;

@Component
public class EndpointParameterMapperRegistry {
  private final Iterable<EndpointParameterMapperFactory> factories;

  public EndpointParameterMapperRegistry(Iterable<EndpointParameterMapperFactory> factories) {
    this.factories = factories;
  }

  public <T> EndpointParameterMapper getEndpointParameterMapperFor(EndpointParamDef endpointParamDef) {
    for (EndpointParameterMapperFactory factory : factories) {
      Optional<? extends EndpointParameterMapper> mapper = factory.getEndpointParameterMapperFor(endpointParamDef);
      if (mapper.isPresent()) {
        return mapper.get();
      }
    }

    throw new IllegalStateException(String.format(
            "no mapper found for parameter %s !%nAvailable factories: %s",
            endpointParamDef,
            Iterables.toString(factories)));
  }
}