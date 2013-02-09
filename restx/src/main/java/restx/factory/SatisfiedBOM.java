package restx.factory;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: xavierhanin
 * Date: 2/9/13
 * Time: 2:14 PM
 */
public class SatisfiedBOM {
    private final BillOfMaterials bom;
    private final ImmutableMap<Factory.Query<?>, Set<?>> materials;

    public SatisfiedBOM(BillOfMaterials bom, ImmutableMap<Factory.Query<?>, Set<?>> materials) {
        this.bom = bom;
        this.materials = materials;
    }

    public BillOfMaterials getBom() {
        return bom;
    }

    public <T> Set<NamedComponent<T>> get(Factory.Query<T> key) {
        return (Set<NamedComponent<T>>) checkNotNull(materials.get(key),
                "key not found: %s. Check your bill of materials. Available keys: %s", key, materials.keySet());
    }

    public <T> Iterable<T> getAsComponents(Factory.Query<T> key) {
        return Iterables.transform(get(key), NamedComponent.<T>toComponent());
    }

    public <T> Optional<NamedComponent<T>> getOne(Factory.Query<T> key) {
        Set<NamedComponent<T>> components = get(key);
        if (components.isEmpty()) {
            return Optional.absent();
        } else if (components.size() == 1) {
            return Optional.of(components.iterator().next());
        } else {
            throw new IllegalStateException(String.format(
                    "more than one component is available for %s." +
                            " Please select which one you want with a more specific query." +
                            " Available components are: %s",
                    key, components));
        }
    }

    @Override
    public String toString() {
        return "SatisfiedBOM{" +
                "bom=" + bom +
                ", materials=" + materials +
                '}';
    }
}
