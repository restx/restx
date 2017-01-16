package restx.factory;

import java.lang.reflect.Type;
import java.util.Set;

/**
 * User: xavierhanin
 * Date: 1/31/13
 * Time: 5:39 PM
 */
public interface FactoryMachine {
    boolean canBuild(Name<?> name);
    <T> MachineEngine<T> getEngine(Name<T> name);
    <T> Set<Name<T>> nameBuildableComponents(Type componentType);
    int priority();
}
