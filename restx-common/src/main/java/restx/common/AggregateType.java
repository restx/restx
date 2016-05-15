package restx.common;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ObjectArrays;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by fcamblor on 13/05/16.
 */
public enum AggregateType {
    ITERABLE() {
        @Override
        public boolean isApplicableTo(String fqcn) {
            return matchesParameterizedFQCN(Iterable.class, fqcn);
        }

        @Override
        public <T> Object createFrom(List values, Class<T> itemClass) {
            return values;
        }
    },
    LIST() {
        @Override
        public boolean isApplicableTo(String fqcn) {
            return matchesParameterizedFQCN(List.class, fqcn);
        }

        @Override
        public <T> Object createFrom(List values, Class<T> itemClass) {
            return values;
        }
    },
    SET() {
        @Override
        public boolean isApplicableTo(String fqcn) {
            return matchesParameterizedFQCN(Set.class, fqcn);
        }

        @Override
        public <T> Object createFrom(List values, Class<T> itemClass) {
            return Sets.newHashSet(values);
        }
    },
    COLLECTION() {
        @Override
        public boolean isApplicableTo(String fqcn) {
            return matchesParameterizedFQCN(Collection.class, fqcn);
        }

        @Override
        public <T> Object createFrom(List values, Class<T> itemClass) {
            return values;
        }
    },
    ARRAY() {
        @Override
        public boolean isApplicableTo(String fqcn) {
            return fqcn.endsWith("[]");
        }

        @Override
        public <T> Object createFrom(List values, Class<T> itemClass) {
            return values.toArray(ObjectArrays.newArray(itemClass, values.size()));
        }
    };

    public static Optional<AggregateType> fromType(String fqcn) {
        for(AggregateType aggregateType : values()){
            if(aggregateType.isApplicableTo(fqcn)) {
                return Optional.of(aggregateType);
            }
        }

        return Optional.absent();
    }

    public abstract boolean isApplicableTo(String fqcn);
    public abstract <T> Object createFrom(List values, Class<T> itemClass);

    protected static boolean matchesParameterizedFQCN(Class c, String fqcn) {
        return fqcn.startsWith(c.getCanonicalName());
    }

    public static boolean isAggregate(String fqcn) {
        return fromType(fqcn).isPresent();
    }
}
