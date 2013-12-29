package restx.jongo;

import com.mongodb.DBObject;
import org.bson.types.ObjectId;
import org.jongo.ResultHandler;

import java.util.regex.Pattern;

/**
 * User: xavierhanin
 * Date: 1/23/13
 * Time: 6:42 PM
 */
public class Jongos {
    public static Pattern startingWith(String expr) {
        return Pattern.compile(String.format("\\Q%s\\E.*", expr));
    }

    public static Pattern ignoreCase(String expr) {
        return Pattern.compile(String.format("\\Q%s\\E", expr), Pattern.CASE_INSENSITIVE);
    }

    public static String newObjectIdKey() {
        return new ObjectId().toString();
    }

    @SuppressWarnings("unchecked")
    public static <T> ResultHandler<T> singleField(final String field, Class<T> fieldClass) {
        return new ResultHandler<T>() {
            @Override
            public T map(DBObject result) {
                return (T) result.get(field);
            }
        };
    }
}
