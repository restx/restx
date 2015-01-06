package restx.config;

import com.google.common.base.Supplier;
import restx.common.RestxConfig;

/**
 * Provide ConfigSupplier to provide a config which is then contributed to the main consolidated config.
 */
public interface ConfigSupplier extends Supplier<RestxConfig> {
}
