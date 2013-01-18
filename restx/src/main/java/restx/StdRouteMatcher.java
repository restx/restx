package restx;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: xavierhanin
 * Date: 1/19/13
 * Time: 7:55 AM
 */
public class StdRouteMatcher implements RestxRouteMatcher {
    private final String method;
    private final String path;

    private final Pattern pattern;
    private final ImmutableList<String> groupNames;

    public StdRouteMatcher(String method, String path) {
        this.method = checkNotNull(method);
        this.path = checkNotNull(path);

        ImmutableList.Builder<String> groupNamesBuilder = ImmutableList.builder();
        Pattern p = Pattern.compile("\\{([^}]+)}");
        Matcher m = p.matcher(path);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String name = m.group(1);
            groupNamesBuilder.add(name);
            m.appendReplacement(sb, Matcher.quoteReplacement("(.+)"));
        }
        m.appendTail(sb);

        pattern = Pattern.compile(sb.toString());
        groupNames = groupNamesBuilder.build();
    }

    @Override
    public Optional<RestxRouteMatch> match(String method, String path) {
        if (!this.method.equals(method)) {
            return Optional.absent();
        }
        Matcher m = pattern.matcher(path);
        if (!m.matches()) {
            return Optional.absent();
        }

        ImmutableMap.Builder<String, String> params = ImmutableMap.builder();
        for (int i = 0; i < m.groupCount() && i < groupNames.size(); i++) {
             params.put(groupNames.get(i), m.group(i + 1));
        }

        return Optional.of(new RestxRouteMatch(path, params.build()));
    }

    @Override
    public String toString() {
        return method + " " + path;
    }
}
