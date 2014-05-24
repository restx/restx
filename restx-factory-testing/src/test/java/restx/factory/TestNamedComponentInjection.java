package restx.factory;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

import javax.inject.Named;
import java.util.Collection;

/**
 * @author xavier hanin
 */
@Module
public class TestNamedComponentInjection {
    @Provides(priority = -10) @Named("NCA") public String a() {
        return "NamedComponentA";
    }

    @Provides @Named("NCB") public String b(@Named("NCA") NamedComponent<String> a) {
        return a.getName().getName() + " " + a.getPriority() + " " + a.getComponent();
    }


    @Provides @Named("NCMA1") public V a1() {
        return new V("NamedComponentA1");
    }

    @Provides(priority = -10) @Named("NCMA2") public V a2() {
        return new V("NamedComponentA2");
    }

    @Provides @Named("NCMB") public String mb(Collection<NamedComponent<V>> as) {
        return Joiner.on(';').join(Iterables.transform(as, new Function<NamedComponent<V>, String>() {
            @Override
            public String apply(NamedComponent<V> a) {
                return a.getName().getName() + " " + a.getPriority() + " " + a.getComponent().getVal();
            }
        }));
    }

    public static class V {
        private final String val;

        public V(String val) {
            this.val = val;
        }

        public String getVal() {
            return val;
        }
    }
}
