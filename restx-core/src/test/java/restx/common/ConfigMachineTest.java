package restx.common;

import com.google.common.base.Optional;
import com.typesafe.config.Config;
import org.junit.Test;
import restx.factory.Factory;
import restx.factory.Name;
import restx.factory.NamedComponent;
import restx.factory.SingletonFactoryMachine;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * User: xavierhanin
 * Date: 9/23/13
 * Time: 9:17 PM
 */
public class ConfigMachineTest {
    @Test
    public void should_load_config() throws Exception {
        System.setProperty("restx.test.exampleKey", "val");
        Factory factory = Factory.builder().addMachine(new FactoryConfigMachine()).build();

        Optional<NamedComponent<Config>> c = factory.queryByName(Name.of(Config.class)).findOne();
        assertThat(c.isPresent()).isTrue();
        assertThat(c.get().getComponent().getString("restx.test.exampleKey")).isEqualTo("val");
    }

    @Test
    public void should_load_config_and_make_string_component_available() throws Exception {
        Factory factory = Factory.builder()
                .addMachine(new FactoryConfigMachine())
                .addMachine(new SingletonFactoryMachine<>(0, NamedComponent.of(String.class, "restx.test.exampleKey2", "val2")))
                .build();

        Optional<NamedComponent<Config>> c = factory.queryByName(Name.of(Config.class)).findOne();
        assertThat(c.isPresent()).isTrue();
        assertThat(c.get().getComponent().getString("restx.test.exampleKey2")).isEqualTo("val2");
    }
}
