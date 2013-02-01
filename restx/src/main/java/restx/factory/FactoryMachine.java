package restx.factory;

import com.google.common.base.Optional;

import java.util.Set;

/**
 * User: xavierhanin
 * Date: 1/31/13
 * Time: 5:39 PM
 */
public interface FactoryMachine {
    <T> Optional<? extends ComponentBox<T>> newComponent(Factory factory, Name<T> name);
    <T> Set<Name<T>> nameBuildableComponents(Class<T> componentClass);
    int priority();
}
