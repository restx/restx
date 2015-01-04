package restx.factory;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

/**
 * @author apeyrard
 */
public interface Warehouse extends AutoCloseable {

    String getId();
    ImmutableList<Warehouse> getProviders();

    <T> Optional<StoredBox<T>> getStoredBox(Name<T> name);

    <T> Optional<NamedComponent<T>> checkOut(Name<T> name);
    <T> void checkIn(ComponentBox<T> componentBox, SatisfiedBOM satisfiedBOM);

    Iterable<Name<?>> listNames();
    Iterable<Name<?>> listDependencies(Name name);

    public void close();

    public static class StoredBox<T> {
        final ComponentBox<T> box;
        final SatisfiedBOM satisfiedBOM;

        public StoredBox(ComponentBox<T> box, SatisfiedBOM satisfiedBOM) {
            this.box = box;
            this.satisfiedBOM = satisfiedBOM;
        }

        public ComponentBox<T> getBox() {
            return box;
        }

        public SatisfiedBOM getSatisfiedBOM() {
            return satisfiedBOM;
        }


        @Override
        public String toString() {
            return "StoredBox{" +
                    "box=" + box +
                    ", satisfiedBOM=" + satisfiedBOM +
                    '}';
        }
    }
}
