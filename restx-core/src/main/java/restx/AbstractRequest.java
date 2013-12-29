package restx;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;

/**
 * Date: 15/11/13
 * Time: 18:38
 */
public abstract class AbstractRequest implements RestxRequest {
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[RESTX REQUEST] ");
        sb.append(getHttpMethod()).append(" ").append(getRestxPath());
        dumpParameters(sb);
        return sb.toString();
    }

    private void dumpParameters(StringBuilder sb) {
        ImmutableMap<String,ImmutableList<String>> queryParams = getQueryParams();
        if (queryParams.isEmpty()) {
            return;
        }
        sb.append(" ? ");
        for (Map.Entry<String, ImmutableList<String>> entry : queryParams.entrySet()) {
            String key = entry.getKey();
            List<String> values = entry.getValue();
            sb.append(key).append("=").append(
                    values.size() == 1
                            ? values.get(0)
                            : Joiner.on("&" + key + "=").join(values));
            sb.append("&");
        }
        sb.setLength(sb.length() - 1);
    }
}
