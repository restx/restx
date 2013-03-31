package restx.factory;

import com.google.common.base.Optional;

/**
* User: xavierhanin
* Date: 3/31/13
* Time: 3:16 PM
*/
public class EmptyBox<T> implements ComponentBox<T> {
    private Name<T> name;

    public EmptyBox(Name<T> name) {
        this.name = name;
    }

    @Override
    public ComponentBox<T> customize(ComponentCustomizer<T> customizer) {
        return this;
    }

    @Override
    public Optional<NamedComponent<T>> pick() {
        return Optional.absent();
    }

    @Override
    public Name<T> getName() {
        return name;
    }

    @Override
    public void close() throws Exception {
    }
}
