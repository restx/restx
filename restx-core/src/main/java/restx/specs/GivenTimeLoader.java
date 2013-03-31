package restx.specs;

import org.joda.time.DateTime;
import restx.factory.Component;

import javax.inject.Named;
import java.util.Date;
import java.util.Map;

/**
* User: xavierhanin
* Date: 3/31/13
* Time: 3:01 PM
*/
@Named("time") @Component
public final class GivenTimeLoader implements RestxSpecLoader.GivenLoader {
    @Override
    public RestxSpec.Given load(Map given1) {
        Object time = given1.get("time");
        if (time instanceof String) {
            return new RestxSpec.GivenTime(DateTime.parse((String) time));
        } else if (time instanceof Date) {
            return new RestxSpec.GivenTime(new DateTime(time));
        } else {
            throw new IllegalArgumentException("invalid given time " + given1 + ": " +
                    "unrecognized value type " + time.getClass().getName() + "." +
                    " Was expecting String or Date ");
        }
    }
}
