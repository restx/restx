package restx.factory;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Iterables;

import java.util.Iterator;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: xavierhanin
 * Date: 2/9/13
 * Time: 2:14 PM
 */
public class SatisfiedBOM {
    private final BillOfMaterials bom;
    private final ImmutableMultimap<Factory.Query<?>, NamedComponent<?>> materials;

    public SatisfiedBOM(BillOfMaterials bom, ImmutableMultimap<Factory.Query<?>, NamedComponent<?>> materials) {
        this.bom = bom;
        this.materials = materials;
    }

    public BillOfMaterials getBom() {
        return bom;
    }

    @SuppressWarnings("unchecked")
    public <T> Iterable<NamedComponent<T>> get(Factory.Query<T> key) {
        ImmutableCollection namedComponents = checkNotNull(materials.get(key),
                "key not found: %s. Check your bill of materials. Available keys: %s", key, materials.keySet());
        return (Iterable<NamedComponent<T>>) namedComponents;
    }

    public <T> Iterable<T> getAsComponents(Factory.Query<T> key) {
        return Iterables.transform(get(key), NamedComponent.<T>toComponent());
    }

    public <T> Optional<NamedComponent<T>> getOne(Factory.Query<T> key) {
        Iterator<NamedComponent<T>> components = get(key).iterator();
        if (!components.hasNext()) {
            return Optional.absent();
        }
        NamedComponent<T> component = components.next();
        if (components.hasNext()) {
            throw new IllegalStateException(String.format(
                    "more than one component is available for %s." +
                            " Please select which one you want with a more specific query." +
                            " Available components are: %s",
                    key, materials.get(key)));
        }
        return Optional.of(component);
    }

    public <T> Optional<T> getOneAsComponent(Factory.Query<T> key) {
        Optional<NamedComponent<T>> namedComponent = getOne(key);
        if(namedComponent.isPresent()) {
            return Optional.of(namedComponent.get().getComponent());
        } else {
            return Optional.absent();
        }
    }

    @Override
    public String toString() {
        return "SatisfiedBOM{" +
                "bom=" + bom +
                ", materials=" + materials +
                '}';
    }

    public Iterable<NamedComponent<? extends Object>> getAllComponents() {
        return materials.values();
    }
}
