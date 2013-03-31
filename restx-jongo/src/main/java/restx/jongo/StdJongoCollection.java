package restx.jongo;

import org.jongo.Jongo;
import org.jongo.MongoCollection;
import restx.factory.*;

import java.util.Collections;
import java.util.Set;

/**
 * User: xavierhanin
 * Date: 3/17/13
 * Time: 5:21 PM
 */
public class StdJongoCollection implements JongoCollection {
    private final Jongo jongo;
    private final String name;

    public StdJongoCollection(Jongo jongo, String name) {
        this.jongo = jongo;
        this.name = name;
    }

    @Override
    public MongoCollection get() {
        return jongo.getCollection(name);
    }



    @Override
    public String getName() {
        return name;
    }

}
