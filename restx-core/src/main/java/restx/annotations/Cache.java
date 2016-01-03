package restx.annotations;

import java.util.concurrent.TimeUnit;

/**
 * Defines a cache annotation so resources can be kept in the client local cache
 *
 * Created by ltouati on 15/02/2015.
 */
public @interface Cache {
    long duration();
    TimeUnit unit() default TimeUnit.SECONDS;
}
