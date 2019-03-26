package restx.http;

import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import restx.RestxRequest;
import restx.RestxResponse;
import restx.StdRoute;
import restx.annotations.ExpiresAfter;
import restx.common.MorePeriods;
import restx.description.OperationDescription;
import restx.description.ResourceDescription;
import restx.factory.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

@Component
public class ExpiresHeaderFilter extends EntityRelatedFilter {

    public ExpiresHeaderFilter() {
        super(Predicates.<StdRoute>alwaysTrue(), Predicates.<ResourceDescription>alwaysTrue(),
                new OperationDescription.Matcher().havingAnyAnnotations(ExpiresAfter.class)
        );
    }

    @Override
    protected void onEntityOutput(StdRoute stdRoute, RestxRequest req, RestxResponse resp,
                                  Optional<?> input, Optional<?> output,
                                  ResourceDescription resourceDescription, OperationDescription operationDescription) {

        if(!output.isPresent()) {
            return;
        }

        ExpiresAfter expiresAfterAnn = operationDescription.findAnnotation(ExpiresAfter.class).get();

        Locale currentLocale = Locale.US;
        DateTime expirationDate = DateTime.now().plus(MorePeriods.parsePeriod(expiresAfterAnn.value(), currentLocale));
        String expiresHeaderValue = createRFC1123DateFormat(currentLocale).format(expirationDate.toDate());

        resp.setHeader("Expires", expiresHeaderValue);
    }

    public static DateFormat createRFC1123DateFormat(Locale locale) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", locale);
        dateFormat.setTimeZone(DateTimeZone.UTC.toTimeZone());
        return dateFormat;
    }
}
