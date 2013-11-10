package restx.annotations.processor;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Date: 23/10/13
 * Time: 10:26
 */
public class TypeHelper {
    private static Pattern PARAMETERIZED_TYPE_PATTERN = Pattern.compile("([^\\<]+)\\<(.+)\\>");

    static String getTypeExpressionFor(String type) {
        Matcher matcher = PARAMETERIZED_TYPE_PATTERN.matcher(type);
        if (matcher.matches()) {
            String rawType = matcher.group(1);
            List<String> pTypes = new ArrayList<>();
            for (String pType : Splitter.on(",").trimResults().split(matcher.group(2))) {
                pTypes.add(getTypeExpressionFor(pType));
            }

            return "Types.newParameterizedType(" + rawType + ".class, " + Joiner.on(", ").join(pTypes) + ")";
        } else {
            return type + ".class";
        }
    }
}
