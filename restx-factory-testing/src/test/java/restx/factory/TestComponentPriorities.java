package restx.factory;

import javax.inject.Named;

/**
 * @author xavier hanin
 */
@Module
public class TestComponentPriorities {
    @Provides(priority = 1) @Named("A") public V a() {
        return new V("A");
    }

    @Provides @Named("B") public V b() {
        return new V("B");
    }

    @Provides @Named("C") public V c() {
        return new V("C");
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
