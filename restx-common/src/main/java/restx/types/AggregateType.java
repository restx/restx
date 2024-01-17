package restx.types;

import com.google.common.collect.ObjectArrays;
import com.google.common.collect.Sets;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by fcamblor on 13/05/16.
 */
public interface AggregateType {
    class ITERABLE implements AggregateType {
        @Override
        public boolean isApplicableTo(String fqcn) {
            return Types.matchesParameterizedFQCN(Iterable.class, fqcn);
        }

        @Override
        public <T> Object createFrom(List values, Class<T> itemClass) {
            return values;
        }

        @Override
        public <T> Object immutableEmptyInstance(Class<T> itemClass) {
            return Collections.emptyList();
        }
    }
    class LIST implements AggregateType {
        @Override
        public boolean isApplicableTo(String fqcn) {
            return Types.matchesParameterizedFQCN(List.class, fqcn);
        }

        @Override
        public <T> Object createFrom(List values, Class<T> itemClass) {
            return values;
        }

        @Override
        public <T> Object immutableEmptyInstance(Class<T> itemClass) {
            return Collections.emptyList();
        }
    }
    class SET implements AggregateType {
        @Override
        public boolean isApplicableTo(String fqcn) {
            return Types.matchesParameterizedFQCN(Set.class, fqcn);
        }

        @Override
        public <T> Object createFrom(List values, Class<T> itemClass) {
            return Sets.newHashSet(values);
        }

        @Override
        public <T> Object immutableEmptyInstance(Class<T> itemClass) {
            return Collections.emptySet();
        }
    }
    class COLLECTION implements AggregateType {
        @Override
        public boolean isApplicableTo(String fqcn) {
            return Types.matchesParameterizedFQCN(Collection.class, fqcn);
        }

        @Override
        public <T> Object createFrom(List values, Class<T> itemClass) {
            return values;
        }

        @Override
        public <T> Object immutableEmptyInstance(Class<T> itemClass) {
            return Collections.emptySet();
        }
    }
    class ARRAY implements AggregateType {
        @Override
        public boolean isApplicableTo(String fqcn) {
            return fqcn.endsWith("[]");
        }

        @Override
        public <T> Object createFrom(List values, Class<T> itemClass) {
            return values.toArray(ObjectArrays.newArray(itemClass, values.size()));
        }

        @Override
        public <T> Object immutableEmptyInstance(Class<T> itemClass) {
            return Array.newInstance(itemClass, 0);
        }
    };


    boolean isApplicableTo(String fqcn);
    <T> Object createFrom(List values, Class<T> itemClass);
    <T> Object immutableEmptyInstance(Class<T> itemClass);
}
