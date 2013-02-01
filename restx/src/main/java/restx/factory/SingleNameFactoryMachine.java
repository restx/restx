package restx.factory;

import com.google.common.base.Optional;

import java.util.Collections;
import java.util.Set;

/**
 * User: xavierhanin
 * Date: 1/31/13
 * Time: 7:23 PM
 */
public abstract class SingleNameFactoryMachine<C> implements FactoryMachine {
    private final int priority;
    private final ComponentBox.BoxFactory boxFactory;
    protected final Name<C> name;

    public SingleNameFactoryMachine(int priority, Name<C> name, ComponentBox.BoxFactory boxFactory) {
        this.priority = priority;
        this.name = name;
        this.boxFactory = boxFactory;
    }

    protected abstract C doNewComponent(Factory factory);

    @Override
    public <T> Optional<? extends ComponentBox<T>> newComponent(Factory factory, Name<T> name) {
        if (this.name.equals(name)) {
            return (Optional) Optional.of(boxFactory.of(new NamedComponent<>(this.name, doNewComponent(factory))));
        } else {
            return Optional.absent();
        }
    }

    @Override
    public Set nameBuildableComponents(Class componentClass) {
        if (componentClass.isAssignableFrom(name.getClazz())) {
            return Collections.singleton(name);
        } else {
            return Collections.emptySet();
        }
    }

    @Override
    public int priority() {
        return priority;
    }
}
