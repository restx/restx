package restx.common;

import com.google.common.base.Function;

public class MoreFunctions {
    public static <A, B extends A> Function<A, B> cast(Class<B> clazz) {
        return new Function<A, B>() {
            @Override
            public B apply(A input) {
                return (B)input;
            }
        };
    }
}
