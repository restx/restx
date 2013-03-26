package restx.jongo;

import com.google.common.base.Supplier;
import org.jongo.MongoCollection;

/**
 * User: xavierhanin
 * Date: 3/17/13
 * Time: 5:59 PM
 */
public interface JongoCollection extends Supplier<MongoCollection> {
    String getName();

}
