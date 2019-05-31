package restx.factory;

import java.util.LinkedHashSet;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * Date: 16/11/13
 * Time: 21:24
 */
public class WarehouseProvidersMachine implements FactoryMachine {
    private final ImmutableList<Warehouse> providers;

    public WarehouseProvidersMachine(ImmutableList<Warehouse> providers) {
        this.providers = providers;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean canBuild(Name<?> name) {
        for (Warehouse provider : providers) {
        	for (Name n: provider.listNames()) {
        		if(Predicates.equalTo(name).apply(n))
        			return true;
        		
        	}
            
        }

        return false;
    }

    @Override
    public <T> MachineEngine<T> getEngine(final Name<T> name) {
        final Optional<NamedComponent<T>> component = findComponent(name);
        if (!component.isPresent()) {
            throw new IllegalArgumentException("can't build " + name);
        }

        final BillOfMaterials bom = findBomFor(name);

        return new MachineEngine<T>() {
            @Override
            public Name<T> getName() {
                return name;
            }

            @Override
            public int getPriority() {
                return component.get().getPriority();
            }

            @Override
            public BillOfMaterials getBillOfMaterial() {
                return bom;
            }

            @Override
            public ComponentBox<T> newComponent(SatisfiedBOM satisfiedBOM) {
                throw new UnsupportedOperationException(
                        "WarehouseProvidersMachine Engine should never be used to actually build components, " +
                                "they should be picked up in the warehouse directly.");
            }

            @Override
            public String toString() {
                return WarehouseProvidersMachine.this.toString() + "Engine";
            }
        };
    }

    private <T> BillOfMaterials findBomFor(Name<T> name) {
        for (Warehouse provider : providers) {
            Optional<Warehouse.StoredBox<T>> storedBox = provider.getStoredBox(name);
            if (storedBox.isPresent()) {
                return storedBox.get().getSatisfiedBOM().getBom();
            }
        }

        throw new IllegalArgumentException("name not found " + name);
    }

    private <T> Optional<NamedComponent<T>> findComponent(Name<T> name) {
        for (Warehouse provider : providers) {
            Optional<NamedComponent<T>> componentOptional = provider.checkOut(name);
            if (componentOptional.isPresent()) {
                return componentOptional;
            }
        }
        return Optional.absent();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Set<Name<T>> nameBuildableComponents(Class<T> componentClass) {
        Set<Name<T>> names = new LinkedHashSet<>();
        for (Warehouse provider : providers) {
            for (Name<?> name : provider.listNames()) {
                if (componentClass.isAssignableFrom(name.getClazz())) {
                    names.add((Name<T>) name);
                }
            }
        }

        return names;
    }

    @Override
    public int priority() {
        return -10000;
    }

    @Override
    public String toString() {
        return "WarehouseProvidersMachine{" +
                "providers=" + providers +
                '}';
    }
}
